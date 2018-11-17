package tz.co.nezatech.neighborapp.signup;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONObject;
import tz.co.nezatech.neighborapp.MainActivity;
import tz.co.nezatech.neighborapp.R;
import tz.co.nezatech.neighborapp.pref.UserPreference;
import tz.co.nezatech.neighborapp.util.ApiUtil;
import tz.co.nezatech.neighborapp.util.CameraUtil;
import tz.co.nezatech.neighborapp.util.ImageConverter;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProfileInfoActivity extends AppCompatActivity {
    private static final int RC_CAMERA_IMAGE_CAPTURE = 500;
    private static final int RC_GALLERY_PICK_PHOTO = 501;
    private static final int RC_PERMISSION_WRITE_EXTERNAL = 100;
    private final String SAVE_MEMBER_URL = ApiUtil.BASE_URL + "/signup/saveuser";
    private final String TAG = ProfileInfoActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);
        init();
    }

    PopupWindow mPopupWindow;
    ImageView btnProfilePic;

    private void init() {
        final String msisdn = getIntent().getExtras().getString(VerifyCodeActivity.DATA_MSISDN).replaceAll("\\+", "");
        final String token = getIntent().getExtras().getString(VerifyCodeActivity.DATA_TOKEN).replaceAll("\\+", "");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
       final String fcmId= preferences.getString(UserPreference.USER_FCM_ID, "");
        final String fcmToken= preferences.getString(UserPreference.USER_FCM_TOKEN, "");

        ActivityCompat.requestPermissions(ProfileInfoActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RC_PERMISSION_WRITE_EXTERNAL);
        final LinearLayout mainLayout = findViewById(R.id.mainLayout);
        btnProfilePic = findViewById(R.id.btnProfilePic);
        btnProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) ProfileInfoActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
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

        Button nextBtn = findViewById(R.id.btnNext);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et = findViewById(R.id.userName);
                String name = et.getText().toString();
                new SaveProfileTask(msisdn, name, token, fcmId, fcmToken).execute();
            }
        });

    }

    private void camera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, RC_CAMERA_IMAGE_CAPTURE);
            closePhotoWindow();
        }

    }

    private void gallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, RC_GALLERY_PICK_PHOTO);
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
        private String token;
        private String fcmId;
        private String fcmToken;
        private String msisdn;

        public SaveProfileTask(String msisdn, String name, String token, String fcmId, String fcmToken) {
            this.msisdn = msisdn.startsWith("+") ? msisdn : "+" + msisdn;
            this.name = name;
            this.token = token;
            this.fcmId = fcmId;
            this.fcmToken = fcmToken;
            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Response doInBackground(Void... params) {
            try {

                URL url = new URL(SAVE_MEMBER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("msisdn", msisdn);
                jsonParam.put("name", name);
                jsonParam.put("token", token);
                jsonParam.put("fcmId", fcmId);
                jsonParam.put("fcmToken", fcmToken);

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
                EditText userName = findViewById(R.id.userName);

                UserPreference p = new UserPreference(ProfileInfoActivity.this);
                Map<String, String> params = new LinkedHashMap<>();

                params.put(UserPreference.USER_STATUS, "Active");
                params.put(UserPreference.USER_NAME, userName.getText().toString());
                params.put(UserPreference.USER_MSISDN, msisdn);
                p.save(params);
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
