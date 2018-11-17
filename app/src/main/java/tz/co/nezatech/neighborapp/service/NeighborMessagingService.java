package tz.co.nezatech.neighborapp.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import tz.co.nezatech.neighborapp.R;
import tz.co.nezatech.neighborapp.call.InCallActivity;
import tz.co.nezatech.neighborapp.pref.UserPreference;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Random;

public class NeighborMessagingService extends FirebaseMessagingService {
    static final String TAG = NeighborMessagingService.class.getSimpleName();
    private static final String ADMIN_CHANNEL_ID = "tz.co.nezatech.neighborapp";
    NotificationManager notificationManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Notification received");
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Setting up Notification channels for android O and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels();
        }
        int notificationId = new Random().nextInt(60000);
        String base64 = remoteMessage.getData().get("message");
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        try {
            String text = new String(data, "UTF-8");
            String[] token = text.split(":");
            String id = token[0];
            String name = token[1];
            String msisdn = token[2];
            //String msg = String.format("%s - %s has sent panic alert. Kindly assist him", msisdn, name);

            Intent intent = panicAlertActivity(remoteMessage, id, name, msisdn);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                    .setContentIntent(panicFromNotiActivity(intent))
                    .setSmallIcon(R.drawable.ic_alert_warnig_24)  //a resource for your custom small icon
                    .setContentTitle(remoteMessage.getData().get("title")) //the "title" value you sent in your notification
                    .setContentText(base64)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationId, notificationBuilder.build());

            startActivity(intent);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private PendingIntent panicFromNotiActivity(Intent notifyIntent) {
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Intent panicAlertActivity(RemoteMessage msg, String id, String name, String msisdn) {
        String from = msg.getFrom();
        Map<String, String> data = msg.getData();

        Intent intent = new Intent(getApplicationContext(), InCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Data: " + data.toString());
        intent.putExtra("id", id);
        intent.putExtra("name", name);
        intent.putExtra("msisdn", msisdn);
        //startActivity(intent);

        return intent;
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, String.format("Refreshed token: %s", token));
        fcmInstanceId();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels() {
        CharSequence adminChannelName = "NeighborApp";
        String adminChannelDescription = "Admin channel desc. NeighborApp";

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    private void fcmInstanceId() {
        Task<InstanceIdResult> instanceId = FirebaseInstanceId.getInstance().getInstanceId();
        instanceId.addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String id = instanceIdResult.getId();
                String token = instanceIdResult.getToken();
                Log.d(TAG, String.format("Instance ID: %s; Refreshed token: %s", id, token));
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                preferences.edit().putString(UserPreference.USER_FCM_TOKEN, token).apply();
                preferences.edit().putString(UserPreference.USER_FCM_ID, id).apply();
            }
        });
    }
}
