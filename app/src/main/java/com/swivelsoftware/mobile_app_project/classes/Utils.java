package com.swivelsoftware.mobile_app_project.classes;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;

public class Utils {
    static public String getSharedPrefString(Context context, String name, String key) {
        return context.getSharedPreferences(name, MODE_PRIVATE).getString(key, "");
    }

    static public String getBaseUrl(Context context) {
        return getSharedPrefString(context, "APIUrl", "apiUrl");
    }
}
