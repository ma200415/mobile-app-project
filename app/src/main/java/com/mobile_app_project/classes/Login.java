package com.mobile_app_project.classes;

import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class Login {
    EditText email, password;

    public Login(EditText email, EditText password) {
        this.email = email;
        this.password = password;
    }

    public boolean isValid() {
        boolean valid = true;

        EditText[] requiredInput = {email, password};

        for (EditText rf : requiredInput) {
            if (rf.getText().toString().trim().equals("")) {
                if (valid) {
                    rf.requestFocus();
                }

                rf.setError("Required");
                valid = false;
            }
        }

        return valid;
    }

    public JSONObject getJSONObject() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("email", email.getText().toString());
            jsonObject.put("password", password.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
