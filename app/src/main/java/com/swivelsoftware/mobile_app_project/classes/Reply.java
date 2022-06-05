package com.swivelsoftware.mobile_app_project.classes;

import org.json.JSONException;
import org.json.JSONObject;

public class Reply {
    public String message, userId, timestamp;

    public Reply(JSONObject jsonObject) {
        try {
            this.userId = jsonObject.getString("userId");
            this.message = jsonObject.getString("message");
            this.timestamp = jsonObject.getString("createTimestamp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
