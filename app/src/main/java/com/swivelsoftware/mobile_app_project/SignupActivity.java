package com.swivelsoftware.mobile_app_project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SignupActivity extends AppCompatActivity {
    EditText firstName, lastName, email, password, reg_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        reg_code = findViewById(R.id.reg_code);
    }

    public void signup(View view) {
        boolean isValid = true;

        EditText[] requiredFields = {firstName, lastName, email, password};

        for (EditText rf : requiredFields) {
            if (rf.getText().toString().trim().equals("")) {
                rf.setError("Required");
                isValid = false;
            }
        }

        if (!isValid) return;

        String _firstName = firstName.getText().toString();
        String _lastName = lastName.getText().toString();
        String _email = email.getText().toString();
        String _password = password.getText().toString();
        String _reg_code = reg_code.getText().toString();


    }

    public void goSignin(View view) {
        this.finish();
    }
}