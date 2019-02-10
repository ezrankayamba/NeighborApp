package tz.co.nezatech.neighborapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import tz.co.nezatech.neighborapp.adapter.GroupAdapter;
import tz.co.nezatech.neighborapp.call.SendPanicActivity;
import tz.co.nezatech.neighborapp.group.ManageGroupActivity;
import tz.co.nezatech.neighborapp.group.NewGroupActivity;
import tz.co.nezatech.neighborapp.model.Group;
import tz.co.nezatech.neighborapp.pref.UserPreference;
import tz.co.nezatech.neighborapp.service.ShakeService;
import tz.co.nezatech.neighborapp.signup.ProfileInfoActivity;
import tz.co.nezatech.neighborapp.signup.Response;
import tz.co.nezatech.neighborapp.signup.WelcomeActivity;
import tz.co.nezatech.neighborapp.util.ApiUtil;

public class MainActivity extends AppCompatActivity {
    public static final String FETCH_GROUPS_URL = ApiUtil.BASE_URL + "/groups/members/";
    public static final String FETCH_SEND_PANIC_URL = ApiUtil.BASE_URL + "/panic";
    public static final String DATA_SELECTED_GROUP = "SelectedGroup";
    private static final String TAG = MainActivity.class.getSimpleName();
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call();
            }
        });

        init();
        fetchGroups();
        startBgShakeService();
    }

    @Override
    protected void onStop() {
        try {
            super.onStop();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void startBgShakeService() {
        Intent intent = new Intent(this, ShakeService.class);
        startService(intent);
    }

    private synchronized void call(){
        Intent intent = new Intent(this, SendPanicActivity.class);
        intent.putExtra(ShakeService.SHOW_ALERT_DIALOG, true);
        getApplication().startActivity(intent);
    }
    private void fetchGroups() {
        UserPreference p = new UserPreference(this);
        String msisdn = p.getString(UserPreference.USER_MSISDN);
        if (msisdn != null) {
            new FetchGroupsTask(msisdn).execute();
        }
    }

    ListView groupList;
    List<Group> groups = new ArrayList<>();
    GroupAdapter customAdapter;
    SwipeRefreshLayout swipeContainer;

    private void init() {
        UserPreference p = new UserPreference(this);
        if (p.getString(UserPreference.USER_STATUS) == null) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
        }

        TextView empty = findViewById(R.id.empty);
        empty.setText(R.string.txt_empty_groups);
        groupList = findViewById(R.id.groupList);
        groupList.setEmptyView(empty);

        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int pos, long l) {
                Group item = (Group) adapterView.getItemAtPosition(pos);
                Intent intent = new Intent(getApplicationContext(), ManageGroupActivity.class);
                intent.putExtra(DATA_SELECTED_GROUP, item);
                startActivity(intent);
            }
        });
        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchGroups();
            }
        });

        groupList.setTextFilterEnabled(true);

        customAdapter = new GroupAdapter(this, R.layout.contacts_list_item, groups);
        groupList.setAdapter(customAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateGroupList(Response response) {
        groups = response.getGroups();
        if (groups == null) {
            groups = new ArrayList<>();
        }
        init();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        TextView empty = findViewById(R.id.empty);
        if (groups.isEmpty()) {
            empty.setText(R.string.txt_empty_groups_nogrps);
        }
        ListView list = findViewById(R.id.groupList);
        list.setEmptyView(empty);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_restart_registration) {
            UserPreference p = new UserPreference(this);
            String fcmId = p.getString(UserPreference.USER_FCM_ID);
            String fcmToken = p.getString(UserPreference.USER_FCM_TOKEN);
            p.removeAll();
            p.save(UserPreference.USER_FCM_ID, fcmId);
            p.save(UserPreference.USER_FCM_TOKEN, fcmToken);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_create_group) {
            Intent intent = new Intent(getApplicationContext(), NewGroupActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class MakeCallTask extends AsyncTask<Void, Void, Response> {
        ProgressBar progressBar;
        private String msisdn;

        public MakeCallTask(String msisdn) {
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
                String urlStr = FETCH_SEND_PANIC_URL;
                Log.d("Url: ", urlStr);
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("msisdn", msisdn);

                Log.i("JSON", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());

                os.flush();
                os.close();

                String resp = conn.getResponseMessage();
                Log.i("MSG", resp);
                int statusCode = conn.getResponseCode();
                Log.i("StatusCode(SP): ", statusCode + "");
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
            swipeContainer.setRefreshing(false);
            if (response.getStatus() == 0) {
                Snackbar.make(progressBar, response.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("Success", null).show();
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
                String urlStr = FETCH_GROUPS_URL + URLEncoder.encode(msisdn.replaceFirst("\\+", ""), "UTF-8");
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
            swipeContainer.setRefreshing(false);
            if (response.getStatus() == 0) {
                updateGroupList(response);
            } else {
                Snackbar.make(progressBar, response.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("ErrorResponse", null).show();
            }
        }
    }
}
