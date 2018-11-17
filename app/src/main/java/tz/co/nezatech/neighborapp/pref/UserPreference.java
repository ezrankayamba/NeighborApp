package tz.co.nezatech.neighborapp.pref;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class UserPreference {
    public static final String USER_STATUS = "user.status";
    public static final String USER_NAME = "user.name";
    public static final String USER_MSISDN = "MSISDN";
    public static final String USER_FCM_TOKEN = "UserFCMToken";
    public static final String USER_FCM_ID = "UserFCMId";
    private static final String NEIGHBOR_PREF = "tz.co.nezatech.neighborapp.preferences";
    SharedPreferences pref;

    public UserPreference(Context context) {
        this.pref = context.getSharedPreferences(NEIGHBOR_PREF, 0);
    }

    public String getString(String name) {
        return this.pref.getString(name, null);
    }
    public void removeAll(){
        SharedPreferences.Editor edit = pref.edit();
        edit.clear();
        edit.commit();
    }

    public void save(String name, String value) {
        SharedPreferences.Editor edit = pref.edit();
        edit.putString(name, value);
        edit.commit();
    }

    public void save(Map<String, String> params) {
        SharedPreferences.Editor edit = pref.edit();
        for (String name : params.keySet()) {
            String value=params.get(name);
            edit.putString(name, value);
        }
        edit.commit();
    }
}
