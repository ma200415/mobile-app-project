package com.swivelsoftware.mobile_app_project.classes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    public String messageId, message, userId, craftId, timestamp;

    public JSONArray replys;

    public Message(JSONObject jsonObject) {
        try {
            this.messageId = jsonObject.getString("_id");
            this.userId = jsonObject.getString("userId");
            this.craftId = jsonObject.getString("craftId");
            this.message = jsonObject.getString("message");
            this.timestamp = jsonObject.getString("createTimestamp");
            this.replys = jsonObject.getJSONArray("replys");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
