package com.swivelsoftware.mobile_app_project.classes;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class Auth {
    Context context;

    final String spName = "user";
    final String spKey = "AUTHTOKEN";

    public Auth(Context context) {
        this.context = context;
    }

    public String getAuthToken() {
        return context.getSharedPreferences(spName, MODE_PRIVATE)
                .getString(spKey, "");
    }

    public void setAuthToken(String token) {
        SharedPreferences pref = context.getSharedPreferences(spName, MODE_PRIVATE);
        pref.edit()
                .putString(spKey, token)
                .apply();
    }

    public void logout() {
        setAuthToken("");
    }
}
