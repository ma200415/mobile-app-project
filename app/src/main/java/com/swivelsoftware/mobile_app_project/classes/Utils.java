package com.swivelsoftware.mobile_app_project.classes;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class Utils {
    final static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    final static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);

    static public String getSharedPrefString(Context context, String name, String key) {
        return context.getSharedPreferences(name, MODE_PRIVATE).getString(key, "");
    }

    static public String getBaseUrl(Context context) {
        return getSharedPrefString(context, "APIUrl", "apiUrl");
    }

    static public Date parseMongoDBDate(String stringDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

        Date date = null;

        try {
            date = dateFormat.parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    static public String formatDate(Date date) {
        return dateFormat.format(date);
    }

    static public String formatDate(String stringDate) {
        Date date = parseMongoDBDate(stringDate);

        return dateFormat.format(date);
    }

    static public String formatDateTime(String stringDate) {
        Date date = parseMongoDBDate(stringDate);

        return dateTimeFormat.format(date);
    }

    static public Bitmap decodeBase64ToBitmap(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    static public String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
        byte[] imageBytes = stream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}
