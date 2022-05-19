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

import org.json.JSONException;
import org.json.JSONObject;

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
                "http://10.0.2.2:3001/signin",
                login.getJSONObject(),
                response -> {
                    try {
                        if (response.has("success") && response.getBoolean("success")) {
                            String authToken = response.getString("authToken");
                            JSONObject userObject = response.getJSONObject("user");
                            JSONObject payloadObject = userObject.getJSONObject("payload");
                            String userID = payloadObject.getString("_id");
                            String lastName = payloadObject.getString("lastName");
                            String firstName = payloadObject.getString("firstName");
                            String email = payloadObject.getString("email");
                            boolean admin = payloadObject.getBoolean("admin");
                            String role = payloadObject.getString("role");

                            Auth auth = new Auth(this);
                            auth.setAuthToken(authToken);
                            auth.setUser(userID, lastName, firstName, email, admin, role);

                            Intent intent = new Intent();
                            intent.putExtra("ACTION", "login");
                            setResult(RESULT_OK, intent);

                            this.finish();
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
                error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
        );

        queue.add(jsonObjectRequest);
    }

    public void goSignup(View view) {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
}