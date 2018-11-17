package tz.co.nezatech.neighborapp.call;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import tz.co.nezatech.neighborapp.R;
import tz.co.nezatech.neighborapp.signup.VerifyCodeActivity;

public class InCallActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_UNLOCK_SCREEN_FOR_ALERTS = 100;

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_in_call);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        init();
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WAKE_LOCK}, PERMISSIONS_REQUEST_UNLOCK_SCREEN_FOR_ALERTS);
        }

        Bundle data = getIntent().getExtras();
        if(data !=null) {
            final String msisdn = data.getString("msisdn");
            final String name = data.getString("name");
            final String id = data.getString("id");

            TextView tvMsisdn = findViewById(R.id.msisdn);
            tvMsisdn.setText(msisdn);
            TextView tvName = findViewById(R.id.name);
            tvName.setText(name);
        }
    }
}
