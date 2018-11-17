package tz.co.nezatech.neighborapp.call;

import android.os.Bundle;
import android.telecom.CallAudioState;
import android.telecom.Connection;
import android.util.Log;

public class NeighborConnection extends Connection {
    private static final String TAG = NeighborConnection.class.getSimpleName();
    public NeighborConnection() {
        super();
    }

    @Override
    public void onShowIncomingCallUi() {
        super.onShowIncomingCallUi();
        Log.d(TAG, "onShowIncoming Ui");
    }

    @Override
    public void onCallAudioStateChanged(CallAudioState state) {
        super.onCallAudioStateChanged(state);
        Log.d(TAG, "onCallAudioStateChanged");
    }

    @Override
    public void onCallEvent(String event, Bundle extras) {
        super.onCallEvent(event, extras);
        Log.d(TAG, "onCallEvent");
    }

    @Override
    public void onSeparate() {
        super.onSeparate();
        Log.d(TAG, "onSeparate");
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
        Log.d(TAG, "onDisconnect");
    }

    @Override
    public void onAbort() {
        super.onAbort();
        Log.d(TAG, "onAbort");
    }

    @Override
    public void onAnswer() {
        super.onAnswer();
        Log.d(TAG, "onAnswer");
    }

    @Override
    public void onReject() {
        super.onReject();
        Log.d(TAG, "onReject");
    }
}
