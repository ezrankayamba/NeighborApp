package tz.co.nezatech.neighborapp.signup;

import android.util.Log;
import com.google.gson.Gson;
import tz.co.nezatech.neighborapp.model.Group;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;

public class Response {
    private int status, smsResultCode;
    private String message;
    private long id;
    private List<Group> groups;

    public static Response read(HttpURLConnection conn) throws IOException {
        InputStream inputStream = conn.getInputStream();

        StringBuffer buffer = new StringBuffer();

        if (inputStream == null) {
            return null;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line + "\n");
        }

        if (buffer.length() == 0) {
            return null;
        }
        conn.disconnect();

        Gson gson = new Gson();
        Log.d("Response", buffer.toString());
        return gson.fromJson(buffer.toString(), Response.class);
    }

    public Response() {
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSmsResultCode() {
        return smsResultCode;
    }

    public void setSmsResultCode(int smsResultCode) {
        this.smsResultCode = smsResultCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
