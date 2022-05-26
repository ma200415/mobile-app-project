package com.swivelsoftware.mobile_app_project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Pair;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.swivelsoftware.mobile_app_project.classes.Auth;
import com.swivelsoftware.mobile_app_project.classes.Craft;
import com.swivelsoftware.mobile_app_project.databinding.ActivityEditCraftBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditCraftActivity extends AppCompatActivity {
    ActivityEditCraftBinding binding;

    final String[] stores = new String[]{};

    Auth auth;
    Craft craft;

    String mode;

    private final int locationRequestCode = 1000;
    private double wayLatitude = 0.0, wayLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditCraftBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = new Auth(this);

        final ArrayAdapter<String> storeAdapter = new ArrayAdapter<>(this, R.layout.craft_store_list_item, stores);

        binding.inputStore.setAdapter(storeAdapter);

        Intent intent = getIntent();

        mode = intent.getStringExtra("mode");

        switch (mode) {
            case Craft.addMode:
                binding.craftMode.setText(R.string.add);
                break;
            case Craft.editMode:
                binding.craftMode.setText(R.string.edit);

                if (intent.hasExtra("craftJson")) {
                    try {
                        JSONObject craftJson = new JSONObject(intent.getStringExtra("craftJson"));

                        craft = new Craft(craftJson);

                        binding.inputName.setEnabled(false);
                        binding.inputName.setText(craft.name);
                        binding.inputStore.setText(craft.store, false);
                        binding.inputDate.setText(craft.date);
                        binding.inputDescription.setText(craft.description);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }

        binding.craftDate.setEndIconOnClickListener(event -> showDatePicker());

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);

        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    wayLatitude = location.getLatitude();
                    wayLongitude = location.getLongitude();

                    Toast.makeText(this, wayLatitude + " : " + wayLongitude, Toast.LENGTH_LONG).show();
                }
            });
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean networkEnabled = false;

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setMaxWaitTime(5000);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        locationRequest.setPriority(priority);
        LocationCallback locationCallback =
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Location lastLocation = locationResult.getLastLocation();
                        if (lastLocation == null) {
                            Log.d("-----", lastLocation + "");
                            Toast.makeText(getBaseContext(), lastLocation+"", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("-----", lastLocation + "");
                            Toast.makeText(getBaseContext(), lastLocation+"", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onLocationAvailability(LocationAvailability availability) {
                        if (!availability.isLocationAvailable()) {
                            Log.d("++++++", availability + "");
                            Toast.makeText(getBaseContext(), availability+"", Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    public void showDatePicker() {
        MaterialDatePicker<Pair<Long, Long>> dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Select dates")
                        .build();

        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar fromCal = Calendar.getInstance();
            Calendar toCal = Calendar.getInstance();

            fromCal.setTimeInMillis(selection.first);
            toCal.setTimeInMillis(selection.second);

            binding.inputDate.setText(fromCal.getTime().toString());
        });

        dateRangePicker.show(getSupportFragmentManager(), "dateRangePicker");
    }

    public void submit(View view) {
        String name = Objects.requireNonNull(binding.inputName.getText()).toString();
        String store = binding.inputStore.getText().toString();
        String date = Objects.requireNonNull(binding.inputDate.getText()).toString();
        String description = Objects.requireNonNull(binding.inputDescription.getText()).toString();

        switch (mode) {
            case Craft.addMode:
                craft = new Craft(name, store, date, description);
                break;
            case Craft.editMode:
                craft = new Craft(craft.id, name, store, date, description);
                break;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                String.format("%s/dog/%s", this.getSharedPreferences("APIUrl", MODE_PRIVATE)
                        .getString("apiUrl", ""), mode),
                craft.getJSONObject(mode),
                response -> {
                    try {
                        if (response.has("success") && response.getBoolean("success")) {
                            finish();
                        } else {
                            Toast.makeText(this, response.toString(), Toast.LENGTH_LONG).show();
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
        ) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("authorization", String.format("Bearer %s", auth.getUserString(Auth.authTokenKey)));
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    private void location() {
//        if (
//                ActivityCompat.checkSelfPermission(
//                        this,
//                        android.Manifest.permission.ACCESS_COARSE_LOCATION
//                ) == PackageManager.PERMISSION_GRANTED ||
//                        ActivityCompat.checkSelfPermission(
//                                this,
//                                android.Manifest.permission.ACCESS_FINE_LOCATION
//                        ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
//                    Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
//        }
//
//        fusedLocationClient.getLastLocation()
//                .addOnSuccessListener(this, location -> {
//                    if (location != null) {
//                        // Logic to handle location object
//                        Toast.makeText(this, location.getLongitude() + " : " + location.getLatitude(), Toast.LENGTH_LONG).show();
//                        Log.d("----", location.getLongitude() + "");
//                        Log.d("----", location.getLatitude() + "");
//                    }
//                });

    }


}