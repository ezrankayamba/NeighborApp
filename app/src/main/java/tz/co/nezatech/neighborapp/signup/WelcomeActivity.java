package tz.co.nezatech.neighborapp.signup;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;
import tz.co.nezatech.neighborapp.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        init();
    }

    private void init() {
        TextView mTermsCondView = findViewById(R.id.mTermsCond);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {

            }

            @Override
            public void updateDrawState(TextPaint textPaint) {
                super.updateDrawState(textPaint);
                textPaint.setColor(ContextCompat.getColor(WelcomeActivity.this, R.color.linkColor));
                textPaint.setUnderlineText(false);
            }
        };
        String termsCond = getResources().getString(R.string.txt_terms_and_conditions);
        SpannableString ss = new SpannableString(termsCond);

        int i = termsCond.indexOf(getResources().getString(R.string.app_name));
        ss.setSpan(clickableSpan, i, termsCond.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //ss.setSpan(new ForegroundColorSpan(Color.BLUE), i, termsCond.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mTermsCondView.setText(ss);
        mTermsCondView.setMovementMethod(LinkMovementMethod.getInstance());

        findViewById(R.id.btnAcceptTerms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomeActivity.this, VerifyPhoneActivity.class);
                startActivity(intent);
            }
        });
    }
}
