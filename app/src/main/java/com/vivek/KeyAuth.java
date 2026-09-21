
package com.vivek;

import android.content.Context;
import android.provider.Settings;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class KeyAuth {
    private String appname;
    private String ownerid;
    private String version;
    private String url;
    private Context context;
    private String sessionid; // Store session ID

    public KeyAuth(String appname, String ownerid, String version, String url, Context context) {
        this.appname = appname;
        this.ownerid = ownerid;
        this.version = version;
        this.url = url;
        this.context = context;
        this.sessionid = null; // Initialize as null
    }

    public void init() throws Exception {
        String initUrl = url + "?type=init&name=" + URLEncoder.encode(appname, "UTF-8") + "&ownerid=" + URLEncoder.encode(ownerid, "UTF-8") + "&ver=" + URLEncoder.encode(version, "UTF-8");
        JSONObject responseJSON = makeApiCall(initUrl);
        if (!responseJSON.getBoolean("success")) {
            throw new Exception(responseJSON.optString("message", "Initialization failed"));
        }
        // Store sessionid from response
        this.sessionid = responseJSON.optString("sessionid", null);
        if (this.sessionid == null || this.sessionid.isEmpty()) {
            throw new Exception("Session ID not provided in init response");
        }
    }

    public UserData login(String username, String password) throws Exception {
        if (sessionid == null || sessionid.isEmpty()) {
            throw new Exception("Session ID not initialized. Call init first.");
        }

        // ✅ HWID fetch and patch for 20+ characters
        String rawHwid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (rawHwid == null || rawHwid.isEmpty()) {
            rawHwid = "defaultandroidid12345";
        }
        String combined = rawHwid + "-vip-panel-hwid-secure";
        String hwid = combined.substring(0, Math.max(20, combined.length()));

        String loginUrl = url + "?type=login" +
                "&username=" + URLEncoder.encode(username, "UTF-8") +
                "&pass=" + URLEncoder.encode(password, "UTF-8") +
                "&hwid=" + URLEncoder.encode(hwid, "UTF-8") +
                "&name=" + URLEncoder.encode(appname, "UTF-8") +
                "&ownerid=" + URLEncoder.encode(ownerid, "UTF-8") +
                "&sessionid=" + URLEncoder.encode(sessionid, "UTF-8");

        JSONObject responseJSON = makeApiCall(loginUrl);
        if (!responseJSON.getBoolean("success")) {
            throw new Exception(responseJSON.optString("message", "Login failed"));
        }
        return new UserData(context, responseJSON);
    }

    public void upgrade(String username, String key) throws Exception {
        if (sessionid == null || sessionid.isEmpty()) {
            throw new Exception("Session ID not initialized. Call init first.");
        }
        String upgradeUrl = url + "?type=upgrade&username=" + URLEncoder.encode(username, "UTF-8") + "&key=" + URLEncoder.encode(key, "UTF-8") + "&name=" + URLEncoder.encode(appname, "UTF-8") + "&ownerid=" + URLEncoder.encode(ownerid, "UTF-8") + "&sessionid=" + URLEncoder.encode(sessionid, "UTF-8");
        JSONObject responseJSON = makeApiCall(upgradeUrl);
        if (!responseJSON.getBoolean("success")) {
            throw new Exception(responseJSON.optString("message", "Upgrade failed"));
        }
    }

    public void license(String key) throws Exception {
        if (sessionid == null || sessionid.isEmpty()) {
            throw new Exception("Session ID not initialized. Call init first.");
        }
        String licenseUrl = url + "?type=license&key=" + URLEncoder.encode(key, "UTF-8") + "&name=" + URLEncoder.encode(appname, "UTF-8") + "&ownerid=" + URLEncoder.encode(ownerid, "UTF-8") + "&sessionid=" + URLEncoder.encode(sessionid, "UTF-8");
        JSONObject responseJSON = makeApiCall(licenseUrl);
        if (!responseJSON.getBoolean("success")) {
            throw new Exception(responseJSON.optString("message", "License activation failed"));
        }
    }

    private JSONObject makeApiCall(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("HTTP error code: " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();

        return new JSONObject(response.toString());
    }

    // Getter for sessionid (optional, for debugging)
    public String getSessionId() {
        return sessionid;
    }
}
