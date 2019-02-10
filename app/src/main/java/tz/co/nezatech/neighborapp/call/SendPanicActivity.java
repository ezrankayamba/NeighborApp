package tz.co.nezatech.neighborapp.call;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import tz.co.nezatech.neighborapp.R;
import tz.co.nezatech.neighborapp.pref.UserPreference;
import tz.co.nezatech.neighborapp.service.ShakeService;
import tz.co.nezatech.neighborapp.signup.ProfileInfoActivity;
import tz.co.nezatech.neighborapp.signup.Response;
import tz.co.nezatech.neighborapp.util.ApiUtil;

public class SendPanicActivity extends AppCompatActivity {
    private static final String TAG = SendPanicActivity.class.getSimpleName();
    private static final String SEND_PANIC_URL = ApiUtil.BASE_URL + "/panic";
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_panic);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean(ShakeService.SHOW_ALERT_DIALOG)) {
                Log.e(TAG, "Panic alert from service");
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                call();
            }
        }
    }

    private void done(String msg) {
        Log.e(TAG, "Completed handling of panic alert: " + msg);
        //Toast.makeText(this,"Completed handling of panic alert: " + msg, Toast.LENGTH_LONG);
        this.finish();
    }

    boolean callLocked = false;

    private synchronized void call() {
        if (callLocked) {
            Log.e(TAG, "Call: Locked -> ");
            return;
        }
        Log.e(TAG, "Call: Making new alert call");
        callLocked = true;

        UserPreference p = new UserPreference(this);
        final String msisdn = p.getString(UserPreference.USER_MSISDN);
        if (msisdn != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Panic Alert")
                    .setMessage(R.string.alert_panic_text)
                    .setIcon(R.drawable.ic_alert_warnig_48)
                    .setPositiveButton(R.string.send_alert_positive_text, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            switch (whichButton) {
                                case AlertDialog.BUTTON_POSITIVE: {
                                    new MakeCallTask(msisdn).execute();
                                }
                                break;
                                default: {

                                }
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            done("Dismissed");
                        }
                    }).show();
        } else {
            Log.e(TAG, "Call: No call is made as there is no msisdn: " + msisdn);
        }
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
                String urlStr = SEND_PANIC_URL;
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
            } finally {
                done("Finished sending the panic alert");
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
            callLocked = false;
            if (response.getStatus() == 0) {
                Snackbar.make(progressBar, response.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("Success", null).show();
            } else {
                Snackbar.make(progressBar, response.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("ErrorResponse", null).show();
            }
        }
    }

}
