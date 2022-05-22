package com.swivelsoftware.mobile_app_project.classes;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class Auth {
    Context context;

    final private String spName = "user";

    final public String authTokenKey = "AUTHTOKEN";
    final public String userIDKey = "USERID";
    final public String lastNameKey = "LASTNAME";
    final public String firstNameKey = "FIRSTNAME";
    final public String emailKey = "EMAIL";
    final public String adminKey = "ADMIN";
    final public String roleKey = "ROLE";

    final public String roleEmployee = "employee";
    final public String rolePublic = "public";

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
}
