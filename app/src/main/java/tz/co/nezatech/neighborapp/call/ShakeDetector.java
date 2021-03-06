package tz.co.nezatech.neighborapp.call;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ShakeDetector implements SensorEventListener {

    public static final float SHAKE_THRESHOLD_GRAVITY = 2.5F;//original 2.7
    public static final int SHAKE_SLOP_TIME_MS = 500;
    public static final int SHAKE_COUNT_RESET_TIME_MS = 3000;
    public static final int SHAKE_ATTEND_RESET_TIME_MS = 6000; //1 minute

    private OnShakeListener mListener;
    private long mShakeTimestamp;
    private int mShakeCount;
    private static String TAG = ShakeDetector.class.getSimpleName();

    public void setOnShakeListener(OnShakeListener listener) {
        this.mListener = listener;
    }

    public interface OnShakeListener {
        public void onShake(int count);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignore
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
       handleTriggeredShake(event);
    }

    public static float gForce(SensorEvent event){
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        // gForce will be close to 1 when there is no movement.
        return (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);
    }

    private synchronized void handleTriggeredShake(SensorEvent event) {
        if (mListener != null) {
            float gForce = gForce(event);

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {

                final long now = System.currentTimeMillis();
                // ignore shake events too close to each other (500ms)
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    //Log.d(TAG, "Too quick");
                    return;
                }

                Log.d(TAG, "gForce: " + gForce);

                // reset the shake count after 3 seconds of no shakes
                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    mShakeCount = 0;
                }

                mShakeTimestamp = now;
                mShakeCount++;

                Log.d(TAG, "Count: " + mShakeCount);

                mListener.onShake(mShakeCount);
            }
        }
    }

    public void resetCounter() {
        mShakeCount = 0;
    }
}