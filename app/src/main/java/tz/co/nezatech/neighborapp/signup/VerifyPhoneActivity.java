package tz.co.nezatech.neighborapp.signup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;
import tz.co.nezatech.neighborapp.R;
import tz.co.nezatech.neighborapp.util.ApiUtil;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

public class VerifyPhoneActivity extends AppCompatActivity {

    public static final String DATA_PHONE_NUMBER = "PhoneNumber";
    public static final String DATA_COUNTRY_CODE = "CountryCode";
    private static final String DATA_AUTOREAD_CODE = "AutoReadCode";
    private final String SEND_OTP_URL = ApiUtil.BASE_URL + "/signup/sendotp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        init();
    }

    EditText countryCode;
    EditText phoneNumberET;

    private void init() {
        countryCode = findViewById(R.id.countryCode);
        countryCode.setFocusable(false);
        final String[] countryCodes = getResources().getStringArray(R.array.countryCodes);
        phoneNumberET = findViewById(R.id.phoneNumber);

        Spinner countryNamesSpinner = findViewById(R.id.countryNames);
        countryNamesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                countryCode.setText(countryCodes[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                countryCode.setText(countryCodes[0]);
            }
        });
        final Button nextBtn = findViewById(R.id.btnNext);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = countryCode.getText().toString();
                String phoneNumber = phoneNumberET.getText().toString();

                if (ContextCompat.checkSelfPermission(VerifyPhoneActivity.this, Manifest.permission.RECEIVE_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(VerifyPhoneActivity.this, new String[]{android.Manifest.permission.RECEIVE_SMS}, VerifyCodeActivity.REQUEST_CODE_READ_SMS);
                } else {
                    new SendOTPTask(phoneNumber, code, true).execute();
                }
            }
        });
        nextBtn.setEnabled(false);

        phoneNumberET.addTextChangedListener(new TextWatcher() {
            int len = 0;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = phoneNumberET.getText().toString();
                len = str.length();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable text) {
                String str = text.toString();
                if (Arrays.asList(new Integer[]{3, 7}).contains(str.length()) && len < str.length()) {//len check for backspace
                    text.append(" ");
                }
                if (str.length() == 4 && len > str.length()) {//len check for backspace
                    text.delete(3, 4);
                }

                if (str.length() == 11) {
                    nextBtn.setEnabled(Pattern.compile("\\d{3} \\d{3} \\d{3}").matcher(str).matches());
                }
            }
        });
    }

    void goToVerification(String phoneNum, String code, boolean auto) {
        Intent intent = new Intent(VerifyPhoneActivity.this, VerifyCodeActivity.class);
        intent.putExtra(VerifyPhoneActivity.DATA_PHONE_NUMBER, phoneNum);
        intent.putExtra(VerifyPhoneActivity.DATA_COUNTRY_CODE, code);
        intent.putExtra(VerifyPhoneActivity.DATA_AUTOREAD_CODE, auto ? 1 : 0);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case VerifyCodeActivity.REQUEST_CODE_READ_SMS: {
                String code = countryCode.getText().toString();
                String phoneNum = phoneNumberET.getText().toString();

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //goToVerification(phoneNum, code, true);
                    new SendOTPTask(phoneNum, code, true).execute();
                } else {
                    //goToVerification(phoneNum, code, false);
                    new SendOTPTask(phoneNum, code, false).execute();
                }
                return;
            }
        }
    }

    class SendOTPTask extends AsyncTask<Void, Void, Response> {
        ProgressBar progressBar;
        private String phoneNum;
        private String code;
        private boolean auto;
        String msisdn;

        public SendOTPTask(String phoneNum, String code, boolean auto) {
            this.phoneNum = phoneNum;
            this.code = code;
            this.auto = auto;

            msisdn = String.format("+%s%s", code, phoneNum.replaceAll(" ", ""));
            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Response doInBackground(Void... params) {
            try {
                URL url = new URL(SEND_OTP_URL);
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

                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                String resp = conn.getResponseMessage();
                Log.i("MSG", resp);
                int statusCode = conn.getResponseCode();
                if (statusCode != 200) {
                    conn.disconnect();
                    return null;
                }

                return Response.read(conn);
            } catch (Exception e) {
                Log.e(VerifyPhoneActivity.class.getSimpleName(), e.getMessage());
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
                goToVerification(phoneNum, code, auto);
            } else {
                Snackbar.make(progressBar, response.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("ErrorResponse", null).show();
            }
        }
    }
}
