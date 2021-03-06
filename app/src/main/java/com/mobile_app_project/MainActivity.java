package com.mobile_app_project;

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
import com.mobile_app_project.classes.Auth;
import com.mobile_app_project.classes.Craft;
import com.mobile_app_project.databinding.ActivityMainBinding;

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

        DrawerLayout drawer = binding.drawerLayout;
        navigationView = binding.navView;

        setHeader();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_admin, R.id.nav_bookmark, R.id.nav_message)
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
                            if ("login".equals(data.getStringExtra("ACTION"))) {
                                setHeader();
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        menu.findItem(R.id.action_add_craft).setVisible(!auth.getUserString(Auth.AUTH_TOKEN_KEY).isEmpty() && auth.getUserString(Auth.ROLE_KEY).equals(Auth.ROLE_EMPLOYEE));

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        invalidateOptionsMenu();
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

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    public interface VolleyCallback {
        void onSuccess(JSONObject result);
    }

    private void setBookmarkMenu(boolean bool) {
        navigationView.getMenu().findItem(R.id.nav_bookmark).setVisible(bool);
    }

    private void setAdminMenu(boolean bool) {
        navigationView.getMenu().findItem(R.id.nav_admin).setVisible(bool);
    }

    private void setMessageMenu(boolean bool) {
        navigationView.getMenu().findItem(R.id.nav_message).setVisible(bool);
    }

    public void setHeader() {
        View headerView = navigationView.getHeaderView(0);

        TextView userName = headerView.findViewById(R.id.userName);
        TextView userEmail = headerView.findViewById(R.id.userEmail);
        Button accountAction = headerView.findViewById(R.id.accountAction);

        String authToken = auth.getUserString(Auth.AUTH_TOKEN_KEY);

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
        auth.setAuthToken(null);

        userName.setText(getString(R.string.welcome));
        userEmail.setText(null);
        accountAction.setText(getString(R.string.action_login));
        accountAction.setOnClickListener(v -> goSignin());

        setAdminMenu(false);
        setBookmarkMenu(false);
        setMessageMenu(false);
    }

    private void setLoginHeader(TextView userName, TextView userEmail, Button accountAction) {
        userName.setText(String.format("%s %s", auth.getUserString(Auth.FIRSTNAME_KEY), auth.getUserString(Auth.LASTNAME_KEY)));
        userEmail.setText(String.format("%s", auth.getUserString(Auth.EMAIL_KEY)));
        accountAction.setText(getString(R.string.action_logout));
        accountAction.setOnClickListener(v -> logout());

        setAdminMenu(auth.getUserBoolean(Auth.ADMIN_KEY));
        setBookmarkMenu(true);
        setMessageMenu(true);
    }

    public void goEditCraft(View view) {
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