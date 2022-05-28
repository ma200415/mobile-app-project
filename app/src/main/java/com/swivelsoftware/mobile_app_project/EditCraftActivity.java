package com.swivelsoftware.mobile_app_project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class EditCraftActivity extends AppCompatActivity {
    ActivityEditCraftBinding binding;

    final String[] stores = new String[]{"Wong Tai Sin", "Tsuen Wan", "Causeway Bay", "Mong Kok"};

    final int locationRequestCode = 1000;

    FusedLocationProviderClient mFusedLocationClient;
    LocationCallback locationCallback;

    Auth auth;
    Craft craft;

    String mode;

    private double wayLatitude = 0.0, wayLongitude = 0.0;

    Geocoder geocoder;

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


        setStoreLocation();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
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

    private void setStoreLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, locationRequestCode);
        } else {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            locationCallback =
                    new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            Location lastLocation = locationResult.getLastLocation();

                            List<Address> addresses;

                            geocoder = new Geocoder(getBaseContext(), Locale.getDefault());

                            try {
                                addresses = geocoder.getFromLocation(
                                        lastLocation.getLatitude(),
                                        lastLocation.getLongitude(),
                                        1);

                                Address address = addresses.get(0);

                                String addressLine = address.getAddressLine(0);

                                String district = addressLine.split(", ")[1];

                                Log.d("+++", district + "");
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            Log.d("-----", lastLocation + "");
                        }

                        @Override
                        public void onLocationAvailability(@NonNull LocationAvailability availability) {
                            if (!availability.isLocationAvailable()) {
                                Log.d("++++++", availability + "");
                                Toast.makeText(getBaseContext(), availability + "", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };

            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    wayLatitude = location.getLatitude();
                    wayLongitude = location.getLongitude();

                    Log.d("-+-+-+-", wayLatitude + " " + wayLongitude);
                }
            });

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            LocationRequest locationRequest = LocationRequest.create()
                    .setMaxWaitTime(5000)
                    .setInterval(10000)
                    .setFastestInterval(5000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }
}