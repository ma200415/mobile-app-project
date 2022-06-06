package com.mobile_app_project.classes;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mobile_app_project.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Message {
    public static String MESSAGE_EXTRA = "101";
    public static String NEW_MESSAGE_EXTRA = "10";

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

    public static void queryMessage(Context context, final MainActivity.VolleyCallback callback, String craftId, Auth auth) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("craftId", craftId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                Utils.getBaseUrl(context) + "/message/craftid",
                jsonObject,
                callback::onSuccess,
                error -> {
                    error.printStackTrace();
                    Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("authorization", String.format("Bearer %s", auth.getUserString(Auth.AUTH_TOKEN_KEY)));
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }
}
