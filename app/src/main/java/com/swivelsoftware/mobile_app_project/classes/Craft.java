package com.swivelsoftware.mobile_app_project.classes;

import org.json.JSONException;
import org.json.JSONObject;

public class Craft {
    final public static String addMode = "add";
    final public static String editMode = "edit";

    String name, store, date, description;

    public Craft(String name, String store, String date, String description) {
        this.name = name;
        this.store = store;
        this.date = date;
        this.description = description;
    }

    public JSONObject getJSONObject() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("name", name);
            jsonObject.put("store", store);
            jsonObject.put("date", date);
            jsonObject.put("description", description);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
