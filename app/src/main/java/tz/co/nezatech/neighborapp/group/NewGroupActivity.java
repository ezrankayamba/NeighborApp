package tz.co.nezatech.neighborapp.group;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import tz.co.nezatech.neighborapp.MainActivity;
import tz.co.nezatech.neighborapp.R;
import tz.co.nezatech.neighborapp.adapter.ContactAdapter;
import tz.co.nezatech.neighborapp.adapter.ContactRecyclerAdapter;
import tz.co.nezatech.neighborapp.model.Contact;
import tz.co.nezatech.neighborapp.model.Group;
import tz.co.nezatech.neighborapp.model.Member;
import tz.co.nezatech.neighborapp.pref.UserPreference;
import tz.co.nezatech.neighborapp.signup.ProfileInfoActivity;
import tz.co.nezatech.neighborapp.signup.Response;

import java.io.DataOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NewGroupActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    public static final String DATA_SELECTED_CONTACTS = "selectedContacts";
    Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Serializable serializable = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            serializable = extras.getSerializable(MainActivity.DATA_SELECTED_GROUP);
        }
        //serializable = Objects.requireNonNull(getIntent().getExtras()).getSerializable(MainActivity.DATA_SELECTED_GROUP);
        if (serializable != null && serializable instanceof Group) {
            group = ((Group) serializable);
            getSupportActionBar().setTitle(group.getName());
        }
        getSupportActionBar().setSubtitle(getString(R.string.txt_add_members));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        initSelected();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    RecyclerView selectedContactList;
    ContactRecyclerAdapter recyclerAdapter;

    private void initSelected() {
        selectedContactList = findViewById(R.id.selectedContactList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.scrollToPosition(0);
        selectedContactList.setLayoutManager(layoutManager);

        selectedContactList.setHasFixedSize(true);
        recyclerAdapter = new ContactRecyclerAdapter(selectedContacts, new ContactRecyclerAdapter.ListUpdateListener() {
            @Override
            public void selected(Contact contact) {
                selectedContacts.remove(contact);
                Snackbar.make(selectedContactList, "Selected item: " + contact.getName(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                updateSelected();
            }
        });
        selectedContactList.setAdapter(recyclerAdapter);
        selectedContactList.setItemAnimator(new DefaultItemAnimator());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (group != null) {
                    new AddGroupMembersTask(group.getId(), selectedContacts).execute();
                } else {
                    Intent intent = new Intent(getApplicationContext(), GroupProfileActivity.class);
                    intent.putExtra(DATA_SELECTED_CONTACTS, (Serializable) selectedContacts);
                    startActivity(intent);
                }
            }
        });
    }

    ListView listView;
    List contactArrayList;
    ArrayAdapter customAdapter;
    List<Contact> selectedContacts = new ArrayList<>();

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            listView = findViewById(R.id.contactList);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView adapterView, View view, int pos, long l) {
                    Contact item = (Contact) adapterView.getItemAtPosition(pos);
                    Toast.makeText(NewGroupActivity.this, item.toString(), Toast.LENGTH_SHORT).show();
                    if (selectedContacts.contains(item)) {
                        item.setSelected(false);
                        selectedContacts.remove(item);
                    } else {
                        //tick
                        item.setSelected(true);
                        selectedContacts.add(item);
                    }
                    updateSelected();
                }
            });
            listView.setTextFilterEnabled(true);

            contactArrayList = new ArrayList<>();

            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Contact contact = new Contact();
                contact.setName(name);
                contact.setMsisdn(phoneNumber);
                contactArrayList.add(contact);
                Log.d("name>>", name + "  " + phoneNumber);
            }
            phones.close();

            customAdapter = new ContactAdapter(this, R.layout.contacts_list_item, contactArrayList);
            listView.setAdapter(customAdapter);
        }
    }

    private void updateSelected() {
        Log.i("UpdateSelected: ", "Size: " + selectedContacts.size());
        recyclerAdapter.notifyDataSetChanged();
        final LinearLayoutManager lm = (LinearLayoutManager) selectedContactList.getLayoutManager();
        int vPos = lm.findLastVisibleItemPosition();
        int lPos = selectedContacts.size() - 1;
        //lm.setStackFromEnd(vPos < lPos);
        //lm.setOrientation(LinearLayout.HORIZONTAL);
        selectedContactList.setLayoutManager(lm);
        selectedContactList.scrollToPosition(lPos);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        TextView empty = findViewById(R.id.empty);
        empty.setText(R.string.txt_no_contact_filter_match);
        ListView list = findViewById(R.id.contactList);
        list.setEmptyView(empty);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init();
            } else {
                Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_group, menu);
        MenuItem mSearch = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setQueryHint("Filter by contact name ...");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                customAdapter.getFilter().filter(newText);
                customAdapter.notifyDataSetChanged();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void updateGroupMembers(Response response) {
        for (Group g : response.getGroups()) {
            if (group.getId() == g.getId()) {
                group = g;
                break;
            }
        }
        Intent intent = new Intent(getApplicationContext(), ManageGroupActivity.class);
        intent.putExtra(MainActivity.DATA_SELECTED_GROUP, group);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    class AddGroupMembersTask extends AsyncTask<Void, Void, Response> {
        ProgressBar progressBar;
        private long id;
        private final List<Contact> contacts;

        public AddGroupMembersTask(long id, List<Contact> contacts) {
            this.id = id;
            this.contacts = contacts;
            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Response doInBackground(Void... params) {
            return saveMembers(id);
        }

        private Response saveMembers(long groupId) {
            try {
                URL url = new URL(GroupProfileActivity.SAVE_MEMBERS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("groudId", id);
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

                Log.i("Response Status", "STATUS: " + String.valueOf(conn.getResponseCode()));
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
                UserPreference p = new UserPreference(NewGroupActivity.this);
                new FetchGroupsTask(p.getString(UserPreference.USER_MSISDN)).execute();
            } else {
                Snackbar.make(progressBar, response.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("ErrorResponse", null).show();
            }
        }
    }

    class FetchGroupsTask extends AsyncTask<Void, Void, Response> {
        ProgressBar progressBar;
        private String msisdn;

        public FetchGroupsTask(String msisdn) {
            this.msisdn = msisdn.startsWith("+") ? msisdn : "+" + msisdn;
            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Response doInBackground(Void... params) {
            return fetch();
        }

        private Response fetch() {
            try {
                String urlStr = MainActivity.FETCH_GROUPS_URL + URLEncoder.encode(msisdn.replaceFirst("\\+", ""), "UTF-8");
                Log.d("Url: ", urlStr);
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                String resp = conn.getResponseMessage();
                Log.i("MSG", resp);
                int statusCode = conn.getResponseCode();
                Log.i("StatusCode", statusCode + "");
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
                updateGroupMembers(response);
            } else {
                Snackbar.make(progressBar, response.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("ErrorResponse", null).show();
            }
        }
    }


}
