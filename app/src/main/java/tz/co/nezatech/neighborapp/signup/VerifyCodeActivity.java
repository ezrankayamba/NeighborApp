package tz.co.nezatech.neighborapp.signup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.*;
import android.text.method.DigitsKeyListener;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.gson.Gson;
import org.json.JSONObject;
import tz.co.nezatech.neighborapp.R;
import tz.co.nezatech.neighborapp.sms.Common;
import tz.co.nezatech.neighborapp.sms.SMSListener;
import tz.co.nezatech.neighborapp.util.ApiUtil;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class VerifyCodeActivity extends AppCompatActivity implements Common.OTPListener {
    public static final String DATA_TOKEN = "OTP_TOKEN";
    public static final int REQUEST_CODE_READ_SMS = 1101;
    public static final String DATA_MSISDN = "MSISDN";
    private final String VALIDATE_OTP_URL = ApiUtil.BASE_URL + "/signup/verifyotp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);
        init();
        otpListener();
    }

    private void otpListener() {
        SMSListener.bindListener(this);
    }

    @Override
    protected void onDestroy() {
        SMSListener.unbindListener();
        super.onDestroy();
    }

    private void init() {
        ActivityCompat.requestPermissions(VerifyCodeActivity.this, new String[]{android.Manifest.permission.RECEIVE_SMS}, REQUEST_CODE_READ_SMS);

        final String phoneNumber = getIntent().getExtras().getString(VerifyPhoneActivity.DATA_PHONE_NUMBER);
        final String countryCode = getIntent().getExtras().getString(VerifyPhoneActivity.DATA_COUNTRY_CODE);
        TextView titleV = findViewById(R.id.verifyCodeTitle);
        String number = countryCode + " " + phoneNumber.replaceFirst("(\\d{3})(\\d{3})(\\d{3})", "$1 $2 $3");
        titleV.setText("Verify +" + number);
        String waitText = String.format(getResources().getString(R.string.txt_wait_verify_code), "+" + number);
        ((TextView) findViewById(R.id.waitText)).setText(waitText);

        TextView mWaitText = findViewById(R.id.waitText);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {

            }

            @Override
            public void updateDrawState(TextPaint textPaint) {
                super.updateDrawState(textPaint);
                textPaint.setColor(ContextCompat.getColor(getApplicationContext(), R.color.linkColor));
                textPaint.setUnderlineText(false);
            }
        };
        //String termsCond = getResources().getString(R.string.txt_wait_verify_code);
        SpannableString ss = new SpannableString(waitText);

        int i = waitText.indexOf("Wrong number?");
        ss.setSpan(clickableSpan, i, waitText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //ss.setSpan(new ForegroundColorSpan(Color.BLUE), i, termsCond.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mWaitText.setText(ss);
        mWaitText.setMovementMethod(LinkMovementMethod.getInstance());

        final EditText codeET = findViewById(R.id.verificationCode);
        codeET.setInputType(InputType.TYPE_CLASS_NUMBER);
        codeET.setKeyListener(DigitsKeyListener.getInstance("0123456789 "));
        codeET.setSingleLine(true);

        final EditText codeTracherET = findViewById(R.id.verificationCodeTracher);
        codeET.addTextChangedListener(new TextWatcher() {
            private static final char space = ' ';
            int len = 0;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = codeET.getText().toString();
                len = str.length();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String curr = codeTracherET.getText().toString();
                StringBuilder sb = new StringBuilder();
                String tmp = charSequence.toString();

                for (int j = 0; j < curr.length(); j++) {
                    if (j >= tmp.length()) {
                        sb.append(j == 3 ? " " : "-");
                    } else {
                        sb.append(" ");
                    }
                }
                codeTracherET.setText(sb.toString());
            }

            @Override
            public void afterTextChanged(Editable text) {
                String str = text.toString();
                if (str.length() == 3 && len < str.length()) {//len check for backspace
                    text.append(" ");
                }
                if (str.length() == 4 && len > str.length()) {//len check for backspace
                    text.delete(3, 4);
                }

                if (str.length() == 7) {
                    new ValidateOTPTask(phoneNumber, countryCode, str.replaceAll(" ", "")).execute();
                }
            }
        });
    }

    @Override
    public void onOTPReceived(String otp) {
        final EditText et = findViewById(R.id.verificationCode);
        et.clearFocus();
        et.setText("");
        final Editable text = et.getText();
        char[] chars = otp.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            text.append(chars[i]);
        }
    }

    class ValidateOTPTask extends AsyncTask<Void, Void, Response> {
        ProgressBar progressBar;
        private String phoneNum;
        private String code;
        private String otp;
        String msisdn;

        public ValidateOTPTask(String phoneNum, String code, String otp) {
            this.phoneNum = phoneNum;
            this.code = code;
            this.otp = otp.replaceAll(" ", "");

            msisdn = String.format("+%s%s", code, phoneNum.replaceAll(" ", ""));
            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Response doInBackground(Void... params) {
            try {

                URL url = new URL(VALIDATE_OTP_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("msisdn", msisdn);
                jsonParam.put("token", otp);

                Log.i("JSON", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());

                os.flush();
                os.close();

                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                String resp = conn.getResponseMessage();
                Log.i("MSG", resp);
                int statusCode = conn.getResponseCode();
                if (statusCode != 200){
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
                Intent intent = new Intent(getApplicationContext(), ProfileInfoActivity.class);
                intent.putExtra(VerifyCodeActivity.DATA_MSISDN, msisdn);
                intent.putExtra(VerifyCodeActivity.DATA_TOKEN, otp);
                startActivity(intent);
            }else {
                Snackbar.make(progressBar, response.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("ErrorResponse", null).show();
            }
        }
    }
}
