package com.swivelsoftware.mobile_app_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.swivelsoftware.mobile_app_project.classes.Auth;
import com.swivelsoftware.mobile_app_project.classes.Login;
import com.swivelsoftware.mobile_app_project.classes.Utils;

import org.json.JSONException;

public class LoginActivity extends AppCompatActivity {
    EditText email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
    }

    public void login(View view) {
        Login login = new Login(email, password);

        if (!login.isValid()) return;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                Utils.getBaseUrl(this) + "/signin",
                login.getJSONObject(),
                response -> {
                    try {
                        if (response.has("success") && response.getBoolean("success")) {
                            String authToken = response.getString("authToken");

                            Auth _auth = new Auth(response.getJSONObject("user").getJSONObject("payload"));

                            Auth auth = new Auth(this);
                            auth.setAuthToken(authToken);
                            auth.setCurrentUser(_auth.userID, _auth.lastName, _auth.firstName, _auth.email, _auth.admin, _auth.role);

                            Intent intent = new Intent();
                            intent.putExtra("ACTION", "login");
                            setResult(RESULT_OK, intent);

                            finish();
                        } else {
                            String message = response.getString("message");

                            switch (response.getString("errorType")) {
                                case "email":
                                    email.setError(message);
                                    email.requestFocus();
                                    break;
                                case "password":
                                    password.setError(message);
                                    password.requestFocus();
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
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
                }
        );

        queue.add(jsonObjectRequest);
    }

    public void goSignup(View view) {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
}