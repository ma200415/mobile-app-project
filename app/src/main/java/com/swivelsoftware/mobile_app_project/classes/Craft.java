package com.swivelsoftware.mobile_app_project.classes;

import org.json.JSONException;
import org.json.JSONObject;

public class Craft {
    final public static String addMode = "add";
    final public static String editMode = "edit";

    public String id, name, store, date, description, addBy, addTimestamp;

    public Craft(String id, String name, String store, String date, String description) {
        this.id = id;
        this.name = name;
        this.store = store;
        this.date = date;
        this.description = description;
    }

    public Craft(String name, String store, String date, String description) {
        this.name = name;
        this.store = store;
        this.date = date;
        this.description = description;
    }

    public Craft(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getString("_id");
            this.name = jsonObject.getString("name");
            this.store = jsonObject.getString("store");
            this.description = jsonObject.getString("description");
            this.date = jsonObject.getString("date");
            this.addBy = jsonObject.getString("addBy");
            this.addTimestamp = jsonObject.getString("addTimestamp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJSONObject(String mode) {
        JSONObject jsonObject = new JSONObject();

        try {
            if (editMode.equals(mode)) {
                jsonObject.put("craftId", id);
            }

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
