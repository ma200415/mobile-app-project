package com.swivelsoftware.mobile_app_project;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.swivelsoftware.mobile_app_project.classes.Auth;
import com.swivelsoftware.mobile_app_project.classes.Craft;
import com.swivelsoftware.mobile_app_project.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;

    NavigationView navigationView;

    ActivityResultLauncher<Intent> mainActivityResultLauncher;

    Auth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String localUrl = "http://10.0.2.2:3001";
        String cloudUrl = "https://mobile-app-project-backend.vincentmichael.repl.co";

        SharedPreferences pref = this.getSharedPreferences("APIUrl", MODE_PRIVATE);
        pref.edit()
                .putString("apiUrl", localUrl)
                .apply();

        auth = new Auth(this);

        setSupportActionBar(binding.appBarMain.toolbar);

        binding.appBarMain.fab.setOnClickListener(view -> startCraft(Craft.addMode));

        DrawerLayout drawer = binding.drawerLayout;
        navigationView = binding.navView;

        setHeader();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        mainActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        if (data != null) {
                            switch (data.getStringExtra("ACTION")) {
                                case "login":
                                    setHeader();
                                    break;
                            }
                        }
                    }
                });
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


    private void goSignin() {
        Intent intent = new Intent(this, LoginActivity.class);
        mainActivityResultLauncher.launch(intent);
    }

    private void logout() {
        auth.setAuthToken("");
        setHeader();
    }

    public interface VolleyCallback {
        void onSuccess(JSONObject result);
    }

    public void setHeader() {
        View headerView = navigationView.getHeaderView(0);

        TextView userName = headerView.findViewById(R.id.userName);
        TextView userEmail = headerView.findViewById(R.id.userEmail);
        Button accountAction = headerView.findViewById(R.id.accountAction);

        String authToken = auth.getUserString(Auth.authTokenKey);

        auth.verifyAuthToken(result -> {
            try {
                if (authToken == null || authToken.equals("")) {
                    setGuestHeader(userName, userEmail, accountAction);
                } else {
                    if (result.has("errorType")) {
                        setGuestHeader(userName, userEmail, accountAction);

                        Toast.makeText(this, result.getString("message"), Toast.LENGTH_LONG).show();
                    } else {
                        setLoginHeader(userName, userEmail, accountAction);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void setGuestHeader(TextView userName, TextView userEmail, Button accountAction) {
        auth.setAuthToken("");

        userName.setText(getString(R.string.welcome));
        userEmail.setText("");
        accountAction.setText(getString(R.string.action_login));
        accountAction.setOnClickListener(v -> goSignin());
    }

    private void setLoginHeader(TextView userName, TextView userEmail, Button accountAction) {
        userName.setText(String.format("%s %s", auth.getUserString(Auth.lastNameKey), auth.getUserString(Auth.firstNameKey)));
        userEmail.setText(String.format("%s", auth.getUserString(Auth.emailKey)));
        accountAction.setText(getString(R.string.action_logout));
        accountAction.setOnClickListener(v -> logout());
    }

    public void goEditCraft(View view) {
        View v = view.getRootView();

        TextView title = v.findViewById(R.id.craft_card_title);
        TextView content = v.findViewById(R.id.craft_card_content);

        startCraft(Craft.editMode);
    }

    public void goAddCraft(MenuItem item) {
        startCraft(Craft.addMode);
    }

    private void startCraft(String mode) {
        Intent intent = new Intent(this, EditCraftActivity.class);
        intent.putExtra("mode", mode);

        startActivity(intent);
    }
}