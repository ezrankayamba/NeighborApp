package tz.co.nezatech.neighborapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tz.co.nezatech.neighborapp.service.ShakeService;

public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, ShakeService.class);
        context.startService(i);
    }
}