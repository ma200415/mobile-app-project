package com.swivelsoftware.mobile_app_project.classes;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.swivelsoftware.mobile_app_project.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Auth {
    Context context;

    final private static String spName = "user";

    final public static String AUTH_TOKEN_KEY = "AUTHTOKEN";
    final public static String USERID_KEY = "_id";
    final public static String LASTNAME_KEY = "lastName";
    final public static String FIRSTNAME_KEY = "firstName";
    final public static String EMAIL_KEY = "email";
    final public static String ADMIN_KEY = "admin";
    final public static String ROLE_KEY = "role";
    final public static String BOOKMARK_KEY = "bookmarks";

    final static public String ROLE_PUBLIC = "public";
    final static public String ROLE_EMPLOYEE = "employee";

    public String userID, lastName, firstName, email, role;
    public boolean admin;
    public JSONArray bookmarks = new JSONArray();

    public Auth(Context context) {
        this.context = context;
    }

    public Auth(JSONObject userObject) {
        try {
            this.userID = userObject.getString(USERID_KEY);
            this.lastName = userObject.getString(LASTNAME_KEY);
            this.firstName = userObject.getString(FIRSTNAME_KEY);
            this.email = userObject.getString(EMAIL_KEY);
            this.role = userObject.getString(ROLE_KEY);
            this.admin = userObject.getBoolean(ADMIN_KEY);
            this.bookmarks = userObject.getJSONArray(BOOKMARK_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setAuthToken(String token) {
        SharedPreferences pref = context.getSharedPreferences(spName, MODE_PRIVATE);
        pref.edit()
                .putString(AUTH_TOKEN_KEY, token)
                .apply();
    }

    public void setCurrentUser(String userID, String lastName, String firstName, String email, boolean admin, String role) {
        SharedPreferences pref = context.getSharedPreferences(spName, MODE_PRIVATE);
        pref.edit()
                .putString(USERID_KEY, userID)
                .putString(LASTNAME_KEY, lastName)
                .putString(FIRSTNAME_KEY, firstName)
                .putString(EMAIL_KEY, email)
                .putBoolean(ADMIN_KEY, admin)
                .putString(ROLE_KEY, role)
                .apply();
    }

    public String getUserString(String key) {
        return context.getSharedPreferences(spName, MODE_PRIVATE)
                .getString(key, "");
    }

    public boolean getUserBoolean(String key) {
        return context.getSharedPreferences(spName, MODE_PRIVATE)
                .getBoolean(key, false);
    }

    public void verifyAuthToken(final MainActivity.VolleyCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                Utils.getBaseUrl(context) + "/auth",
                null,
                callback::onSuccess,
                error -> {
                    error.printStackTrace();
                    Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("authorization", String.format("Bearer %s", getUserString(Auth.AUTH_TOKEN_KEY)));
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    public void queryUserById(final MainActivity.VolleyCallback callback, String userID) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("id", userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                Utils.getBaseUrl(context) + "/user/id",
                jsonObject,
                callback::onSuccess,
                error -> {
                    error.printStackTrace();
                    Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
                }
        );

        queue.add(jsonObjectRequest);
    }
}
