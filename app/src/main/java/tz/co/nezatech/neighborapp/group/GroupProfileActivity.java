package tz.co.nezatech.neighborapp.group;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import tz.co.nezatech.neighborapp.MainActivity;
import tz.co.nezatech.neighborapp.R;
import tz.co.nezatech.neighborapp.adapter.ContactInGroupAdapter;
import tz.co.nezatech.neighborapp.model.Contact;
import tz.co.nezatech.neighborapp.pref.UserPreference;
import tz.co.nezatech.neighborapp.signup.ProfileInfoActivity;
import tz.co.nezatech.neighborapp.signup.Response;
import tz.co.nezatech.neighborapp.util.ApiUtil;
import tz.co.nezatech.neighborapp.util.CameraUtil;
import tz.co.nezatech.neighborapp.util.ImageConverter;

import java.io.DataOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupProfileActivity extends AppCompatActivity {

    private static final int RC_PERMISSION_WRITE_EXTERNAL = 100;
    private final String SAVE_GROUP_URL = ApiUtil.BASE_URL + "/groups/save";
    public static final String SAVE_MEMBERS_URL = ApiUtil.BASE_URL + "/groups/members";
    private final String TAG = ProfileInfoActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setSubtitle(getString(R.string.txt_sub_title_add_name));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    PopupWindow mPopupWindow;
    ImageView btnProfilePic;

    private void init() {
        Serializable serializable = Objects.requireNonNull(getIntent().getExtras()).getSerializable(NewGroupActivity.DATA_SELECTED_CONTACTS);
        final List<Contact> contacts;
        if (serializable != null && serializable instanceof List) {
            contacts = (List<Contact>) serializable;
        } else {
            contacts = new ArrayList<>();
        }
        TextView membersCountText = findViewById(R.id.membersCountText);
        membersCountText.setText(getResources().getString(R.string.txt_members_label) + contacts.size());
        ActivityCompat.requestPermissions(GroupProfileActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RC_PERMISSION_WRITE_EXTERNAL);
        final LinearLayout mainLayout = findViewById(R.id.mainLayout);
        btnProfilePic = findViewById(R.id.btnProfilePic);
        btnProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) GroupProfileActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                View customView = inflater.inflate(R.layout.popup_profile_photo, null);

                mPopupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (Build.VERSION.SDK_INT >= 21) {
                    mPopupWindow.setElevation(5.0f);
                }

                mPopupWindow.showAtLocation(mainLayout, Gravity.BOTTOM, 0, 0);
                Button btnCamera = customView.findViewById(R.id.btnCamera);
                btnCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        camera();
                    }
                });
                Button btnGallery = customView.findViewById(R.id.btnGallery);
                btnGallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gallery();
                    }
                });
            }
        });

        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closePhotoWindow();
            }
        });

        ImageButton nextBtn = findViewById(R.id.btnNext);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et = findViewById(R.id.groupName);
                String name = et.getText().toString();
                UserPreference p = new UserPreference(GroupProfileActivity.this);
                String msisdn = p.getString(UserPreference.USER_MSISDN);
                new SaveProfileTask(msisdn, name, contacts).execute();
            }
        });

        GridView gridview = findViewById(R.id.selectedContactsGrid);
        gridview.setAdapter(new ContactInGroupAdapter(this, R.layout.contacts_list_item_horizontal_noclose, contacts));
    }

    private void camera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CameraUtil.RC_CAMERA_IMAGE_CAPTURE);
            closePhotoWindow();
        }
    }

    private void gallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, CameraUtil.RC_GALLERY_PICK_PHOTO);
        closePhotoWindow();
    }

    private void closePhotoWindow() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
            mPopupWindow = null;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, final Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        CameraUtil.cameraResults(requestCode, resultCode, imageReturnedIntent, this, new CameraUtil.ImageListener() {
            @Override
            public void ready() {
                Bundle extras = imageReturnedIntent.getExtras();
                Bitmap thePic = extras.getParcelable("data");
                Bitmap roundedImg = ImageConverter.getRoundedCorner(thePic, 128);
                btnProfilePic.setImageBitmap(roundedImg);
            }
        });
    }

    class SaveProfileTask extends AsyncTask<Void, Void, Response> {
        ProgressBar progressBar;
        private String name;
        private final List<Contact> contacts;
        private String msisdn;

        public SaveProfileTask(String msisdn, String name, List<Contact> contacts) {
            this.msisdn = msisdn.startsWith("+") ? msisdn : "+" + msisdn;
            this.name = name;
            this.contacts = contacts;
            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Response doInBackground(Void... params) {
            Response res = saveGroup();
            if (res != null && res.getStatus() == 0) {
                return saveMembers(res.getId());
            }
            return res;
        }

        private Response saveGroup() {
            try {

                URL url = new URL(SAVE_GROUP_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("msisdn", msisdn);
                jsonParam.put("name", name);

                Log.i("JSON", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());

                os.flush();
                os.close();

                Log.i(TAG, "STATUS: " + String.valueOf(conn.getResponseCode()));
                String resp = conn.getResponseMessage();
                Log.i("MSG", resp);
                int statusCode = conn.getResponseCode();
                if (statusCode != 200) {
                    conn.disconnect();
                    return null;
                }

                return Response.read(conn);
            } catch (Exception e) {
                Log.e(ProfileInfoActivity.class.getSimpleName(), e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        private Response saveMembers(long groupId) {
            try {

                URL url = new URL(SAVE_MEMBERS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("groudId", groupId);
                JSONArray members = new JSONArray();
                for (Contact c : contacts) {
                    JSONObject o = new JSONObject();
                    String msisdn = c.getMsisdn();
                    if (msisdn.startsWith("0")) {
                        msisdn = msisdn.replaceFirst("0", "+255");
                    }
                    msisdn = msisdn.replaceAll(" ", "").trim();
                    if (!msisdn.startsWith("+")) {
                        msisdn = "+" + msisdn;
                    }

                    o.put("msisdn", msisdn);
                    o.put("name", c.getName());
                    members.put(o);
                }
                jsonParam.put("members", members);

                Log.i("JSON", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());

                os.flush();
                os.close();

                Log.i(TAG, "STATUS: " + String.valueOf(conn.getResponseCode()));
                String resp = conn.getResponseMessage();
                Log.i("MSG", resp);
                int statusCode = conn.getResponseCode();
                if (statusCode != 200) {
                    conn.disconnect();


                    return null;
                }

                return Response.read(conn);
            } catch (Exception e) {
                Log.e(ProfileInfoActivity.class.getSimpleName(), e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Response response) {
            super.onPostExecute(response);
            progressBar.setVisibility(View.INVISIBLE);
            if (response.getStatus() == 0) {
                //UserPreference p = new UserPreference(GroupProfileActivity.this);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Snackbar.make(progressBar, response.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("ErrorResponse", null).show();
            }
        }
    }
}
