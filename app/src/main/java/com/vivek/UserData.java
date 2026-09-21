package com.vivek;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserData {
    private String username;
    private String subscription;
    private String expiry;
    private Context context;

    public UserData(Context context, JSONObject json) {
        this.context = context;
        try {
            if (json.has("info")) {
                JSONObject info = json.getJSONObject("info");
                this.username = info.has("username") ? info.getString("username") : "Unknown";

                if (info.has("subscriptions")) {
                    JSONArray subArray = info.getJSONArray("subscriptions");
                    if (subArray.length() > 0) {
                        JSONObject subObject = subArray.getJSONObject(0);
                        this.subscription = subObject.has("subscription") ? subObject.getString("subscription") : "None";
                        this.expiry = subObject.has("expiry") ? subObject.getString("expiry") : "N/A";
                    } else {
                        this.subscription = "None";
                        this.expiry = "N/A";
                        Toast.makeText(context, "No subscriptions found in JSON data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    this.subscription = "None";
                    this.expiry = "N/A";
                    Toast.makeText(context, "Subscriptions key missing in JSON data", Toast.LENGTH_SHORT).show();
                }
            } else {
                this.username = "Unknown";
                this.subscription = "None";
                this.expiry = "N/A";
                Toast.makeText(context, "Info key missing in JSON data", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            this.username = "Unknown";
            this.subscription = "None";
            this.expiry = "N/A";
            Toast.makeText(context, "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public String getUsername() {
        return username;
    }

    public String getSubscription() {
        return subscription;
    }

    public String getExpiry() {
        return expiry;
    }
}