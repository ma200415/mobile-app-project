package com.swivelsoftware.mobile_app_project;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.swivelsoftware.mobile_app_project.classes.Signup;

import org.json.JSONException;

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
        Signup signup = new Signup(firstName, lastName, email, password, reg_code);

        if (!signup.isValid()) return;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                "http://10.0.2.2:3001/signup",
                signup.getJSONObject(),
                response -> {
                    try {
                        String message = response.getString("message");

                        if (response.has("success") && response.getBoolean("success")) {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            goSignin(view);
                        } else {
                            switch (response.getString("errorType")) {
                                case "email":
                                    email.setError(message);
                                    email.requestFocus();
                                    break;
                                case "firstName":
                                    firstName.setError(message);
                                    firstName.requestFocus();
                                    break;
                                case "lastName":
                                    lastName.setError(message);
                                    lastName.requestFocus();
                                    break;
                                case "password":
                                    password.setError(message);
                                    password.requestFocus();
                                    break;
                                case "code":
                                    reg_code.setError(message);
                                    reg_code.requestFocus();
                                    break;
                                default:
                                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
        );

        queue.add(jsonObjectRequest);
    }

    public void goSignin(View view) {
        this.finish();
    }
}