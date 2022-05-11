package com.swivelsoftware.mobile_app_project.classes;

import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class Signup {
    EditText lastName, firstName, email, password, code;

    public Signup(EditText lastName, EditText firstName, EditText email, EditText password, EditText code) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.password = password;
        this.code = code;
    }

    public boolean isValid() {
        boolean valid = true;

        EditText[] requiredInput = {firstName, lastName, email, password};

        for (EditText rf : requiredInput) {
            if (rf.getText().toString().trim().equals("")) {
                rf.setError("Required");
                valid = false;
            }
        }

        return valid;
    }

    public JSONObject getJSONObject() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("lastName", lastName.getText().toString());
            jsonObject.put("firstName", firstName.getText().toString());
            jsonObject.put("email", email.getText().toString());
            jsonObject.put("password", password.getText().toString());
            jsonObject.put("code", code.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
