package tz.co.nezatech.neighborapp.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;
import tz.co.nezatech.neighborapp.util.RegexUtil;

public class SMSListener extends BroadcastReceiver {

    private static Common.OTPListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();
        Object[] pdus = new Object[0];
        if (data != null) {
            pdus = (Object[]) data.get("pdus"); // the pdus key will contain the newly received SMS
        }

        if (pdus != null) {
            for (Object pdu : pdus) { // loop through and pick up the SMS of interest
                String format = data.getString("format");
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu, format);
                String src = sms.getOriginatingAddress();
                String str = sms.getMessageBody();
                if(!src.equalsIgnoreCase(Common.OTP_SMS_SENDER_ID)){
                    continue;
                }
                String otp = RegexUtil.value(str, Common.OTP_SMS_REGEX);
                if (mListener != null) {
                    mListener.onOTPReceived(otp);
                }
                break;
            }
        }
    }

    public static void bindListener(Common.OTPListener listener) {
        mListener = listener;
    }

    public static void unbindListener() {
        mListener = null;
    }
}
