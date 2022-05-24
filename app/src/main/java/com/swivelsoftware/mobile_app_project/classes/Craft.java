package com.swivelsoftware.mobile_app_project.classes;

import org.json.JSONException;
import org.json.JSONObject;

public class Craft {
    final public static String addMode = "add";
    final public static String editMode = "edit";

    String store, date, remark;

    public Craft(String store, String date, String remark) {
        this.store = store;
        this.date = date;
        this.remark = remark;
    }

    public JSONObject getJSONObject() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("store", store);
            jsonObject.put("date", date);
            jsonObject.put("remark", remark);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
