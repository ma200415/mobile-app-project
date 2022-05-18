package com.swivelsoftware.mobile_app_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.swivelsoftware.mobile_app_project.classes.Auth;
import com.swivelsoftware.mobile_app_project.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;

    ActivityMainBinding binding;

    Auth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = new Auth(this);

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        setHeader(navigationView);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void goSignin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void setHeader(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);

        TextView userName = headerView.findViewById(R.id.userName);
        TextView email = headerView.findViewById(R.id.userEmail);
        Button accountAction = headerView.findViewById(R.id.accountAction);

        String authToken = auth.getAuthToken();
        Log.d("+++", authToken);
        if (authToken.equals("")) {
            userName.setText(getString(R.string.welcome));
            email.setText("");
            accountAction.setText(getString(R.string.action_login));
            accountAction.setOnClickListener(v -> goSignin());
        } else {
            RequestQueue queue = Volley.newRequestQueue(this);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    "http://10.0.2.2:3001/auth",
                    null,
                    response -> {
                        try {
                            if (response.has("success") && response.getBoolean("success")) {
                                JSONObject userPayload = response.getJSONObject("payload");
//todo refresh layout
                                String lastName = userPayload.getString("lastName");
                                String firstName = userPayload.getString("firstName");
                                String userEmail = userPayload.getString("email");
                                String role = userPayload.getString("role");
                                Boolean admin = userPayload.getBoolean("admin");

                                userName.setText(String.format("%s %s", lastName, firstName));
                                email.setText(userEmail);
                                accountAction.setText(getString(R.string.action_logout));
                                accountAction.setOnClickListener(v -> auth.logout());
                            } else {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    String bearer = "Bearer ".concat(authToken);
                    Map<String, String> headersSys = super.getHeaders();
                    Map<String, String> headers = new HashMap<>();
                    headersSys.remove("Authorization");
                    headers.put("Authorization", bearer);
                    headers.putAll(headersSys);
                    return headers;
                }
            };

            queue.add(jsonObjectRequest);
        }
    }
}