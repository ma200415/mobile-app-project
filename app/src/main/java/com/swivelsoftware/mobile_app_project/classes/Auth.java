package com.swivelsoftware.mobile_app_project.classes;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class Auth {
    Context context;

    final private static String spName = "user";

    final public static String authTokenKey = "AUTHTOKEN";
    final public static String userIDKey = "USERID";
    final public static String lastNameKey = "LASTNAME";
    final public static String firstNameKey = "FIRSTNAME";
    final public static String emailKey = "EMAIL";
    final public static String adminKey = "ADMIN";
    final public static String roleKey = "ROLE";

    final public static String roleEmployee = "employee";
    final public static String rolePublic = "public";

    public Auth(Context context) {
        this.context = context;
    }

    public void setAuthToken(String token) {
        SharedPreferences pref = context.getSharedPreferences(spName, MODE_PRIVATE);
        pref.edit()
                .putString(authTokenKey, token)
                .apply();
    }

    public void setUser(String userID, String lastName, String firstName, String email, boolean admin, String role) {
        SharedPreferences pref = context.getSharedPreferences(spName, MODE_PRIVATE);
        pref.edit()
                .putString(userIDKey, userID)
                .putString(lastNameKey, lastName)
                .putString(firstNameKey, firstName)
                .putString(emailKey, email)
                .putBoolean(adminKey, admin)
                .putString(roleKey, role)
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

    public boolean verifyAuthToken() {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                context.getSharedPreferences("APIUrl", MODE_PRIVATE)
                        .getString("apiUrl", "") + "/auth",
                null,
                response -> {
                    Toast.makeText(context, response.toString(), Toast.LENGTH_LONG).show();
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("authorization", String.format("Bearer %s", getUserString(Auth.authTokenKey)));
                return headers;
            }
        };

        queue.add(jsonObjectRequest);

        return false;
    }
}
