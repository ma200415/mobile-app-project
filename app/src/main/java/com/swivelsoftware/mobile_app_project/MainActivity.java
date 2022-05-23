package com.swivelsoftware.mobile_app_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.swivelsoftware.mobile_app_project.classes.Auth;
import com.swivelsoftware.mobile_app_project.databinding.ActivityMainBinding;

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

        auth = new Auth(this);

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
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

    public void setHeader() {
        View headerView = navigationView.getHeaderView(0);

        TextView userName = headerView.findViewById(R.id.userName);
        TextView userEmail = headerView.findViewById(R.id.userEmail);
        Button accountAction = headerView.findViewById(R.id.accountAction);

        String authToken = auth.getUserString(auth.authTokenKey);

        if (authToken == null || authToken.equals("")) {
            userName.setText(getString(R.string.welcome));
            userEmail.setText("");
            accountAction.setText(getString(R.string.action_login));
            accountAction.setOnClickListener(v -> goSignin());
        } else {
            userName.setText(String.format("%s %s", auth.getUserString(auth.lastNameKey), auth.getUserString(auth.firstNameKey)));
            userEmail.setText(String.format("%s", auth.getUserString(auth.emailKey)));
            accountAction.setText(getString(R.string.action_logout));
            accountAction.setOnClickListener(v -> logout());
        }
    }

    public void goEditCraft(View view) {
        Intent intent = new Intent(view.getContext(), EditCraftActivity.class);
        startActivity(intent);
    }
}