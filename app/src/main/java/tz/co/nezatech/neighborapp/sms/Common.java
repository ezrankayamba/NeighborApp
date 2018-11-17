package tz.co.nezatech.neighborapp.sms;

public interface Common {
    String OTP_SMS_SENDER_ID = "AFRICASTKNG";
    String OTP_SMS_REGEX = "^(\\d{6})";

    interface OTPListener {
        void onOTPReceived(String otp);
    }
}
