package tz.co.nezatech.neighborapp.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Arrays;

import tz.co.nezatech.neighborapp.call.SendPanicActivity;
import tz.co.nezatech.neighborapp.call.ShakeDetector;

public class ShakeService extends Service implements SensorEventListener {
    private static final String TAG = ShakeService.class.getSimpleName();
    public static final String SHOW_ALERT_DIALOG = "PanicDialogFromService";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private int mShakeCount = 0;
    private long mShakeTimestamp;

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI, new Handler());
        Log.e(TAG, "Service created");
    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(this);
        Log.e(TAG, "Service destroyed");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float gForce = ShakeDetector.gForce(event);
        final long now = System.currentTimeMillis();

        if (gForce > ShakeDetector.SHAKE_THRESHOLD_GRAVITY) {
            if (mShakeTimestamp + ShakeDetector.SHAKE_SLOP_TIME_MS > now) {
                return;
            }
            if (mShakeTimestamp + ShakeDetector.SHAKE_ATTEND_RESET_TIME_MS > now && mShakeCount == -1) {
                Log.d(TAG, "A pause until previous is attended");
                return;
            }

            if (mShakeTimestamp + ShakeDetector.SHAKE_COUNT_RESET_TIME_MS < now) {
                mShakeCount = 0;
            }

            mShakeTimestamp = now;
            mShakeCount++;
            Log.e(TAG, "Count: " + repeat(mShakeCount));

            if (mShakeCount >= 3) {
                mShakeCount = -1;
                Log.e(TAG, "Ready ...");
                Intent intent = new Intent(this, SendPanicActivity.class);
                intent.putExtra(ShakeService.SHOW_ALERT_DIALOG, true);
                getApplication().startActivity(intent);
            }
        }
    }

    private String repeat(int times) {
        char[] chars = new char[times];
        Arrays.fill(chars, '*');
        return new String(chars);
    }

}