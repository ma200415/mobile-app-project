package com.mobile_app_project;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.mobile_app_project.classes.Auth;
import com.mobile_app_project.classes.Craft;
import com.mobile_app_project.classes.Utils;
import com.mobile_app_project.databinding.ActivityEditCraftBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class EditCraftActivity extends AppCompatActivity {
    ActivityEditCraftBinding binding;

    static final int REQUEST_LOCATION = 1000;

    FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;

    Auth auth;
    Craft craft;

    String mode;

    ActivityResultLauncher<Intent> mainActivityResultLauncher;

    Bitmap craftPhotoBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditCraftBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = new Auth(this);

        Craft.setStores(this, binding.inputStore);

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

                        if (!craft.photo.isEmpty()) {
                            craftPhotoBitmap = Utils.decodeBase64ToBitmap(craft.photo);

                            binding.imagePreview.setImageBitmap(craftPhotoBitmap);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }

        binding.craftDate.setEndIconOnClickListener(event -> showDatePicker());

        mainActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        if (data != null) {
                            Uri imageUrl = data.getParcelableExtra("IMAGE_URL");

                            try {
                                InputStream ims = getContentResolver().openInputStream(imageUrl);

                                craftPhotoBitmap = BitmapFactory.decodeStream(ims);

                                binding.imagePreview.setImageBitmap(craftPhotoBitmap);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();

        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public void showDatePicker() {
        MaterialDatePicker<Pair<Long, Long>> dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Select dates")
                        .build();

        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar startCal = Calendar.getInstance();
            Calendar endCal = Calendar.getInstance();

            startCal.setTimeInMillis(selection.first);
            endCal.setTimeInMillis(selection.second);

            Date startDate = startCal.getTime();
            Date endDate = endCal.getTime();

            String formattedDateRange = String.format("%s - %s", Utils.formatDate(startDate), Utils.formatDate(endDate));

            binding.inputDate.setText(formattedDateRange);
        });

        dateRangePicker.show(getSupportFragmentManager(), "dateRangePicker");
    }

    public void submit(View view) {
        String name = Objects.requireNonNull(binding.inputName.getText()).toString();
        String store = binding.inputStore.getText().toString();
        String date = Objects.requireNonNull(binding.inputDate.getText()).toString();
        String description = Objects.requireNonNull(binding.inputDescription.getText()).toString();
        String photo = "";

        if (craftPhotoBitmap != null) {
            photo = Utils.encodeBitmapToBase64(craftPhotoBitmap);
        }

        switch (mode) {
            case Craft.addMode:
                craft = new Craft(name, store, date, description, photo);
                break;
            case Craft.editMode:
                craft = new Craft(craft.id, name, store, date, description, photo);
                break;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                String.format("%s/dog/%s", Utils.getBaseUrl(this), mode),
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
                headers.put("authorization", String.format("Bearer %s", auth.getUserString(Auth.AUTH_TOKEN_KEY)));
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    public void setLocation(View view) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        } else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            locationCallback =
                    new LocationCallback() {
//                        @Override
//                        public void onLocationResult(@NonNull LocationResult locationResult) {
//                            Location lastLocation = locationResult.getLastLocation();
//
//                            List<Address> addresses;
//
//                            geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
//
//                            try {
//                                addresses = geocoder.getFromLocation(
//                                        lastLocation.getLatitude(),
//                                        lastLocation.getLongitude(),
//                                        1);
//
//                                Address address = addresses.get(0);
//
//                                String addressLine = address.getAddressLine(0);
//
//                                String district = addressLine.split(", ")[1];
//
//                                Log.d("+++", district + "");
//
//                                if (Arrays.asList(stores).contains(district)) {
//                                    binding.inputStore.setText(district);
//                                }
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
//                            }
//
//                            Log.d("-----", lastLocation + "");
//                        }
//
//                        @Override
//                        public void onLocationAvailability(@NonNull LocationAvailability availability) {
//                            if (!availability.isLocationAvailable()) {
//                                Log.d("++++++", availability + "");
//                                Toast.makeText(getBaseContext(), availability + "", Toast.LENGTH_SHORT).show();
//                            }
//                        }
                    };

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    List<Address> addresses;

                    Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());

                    try {
                        addresses = geocoder.getFromLocation(
                                location.getLatitude(),
                                location.getLongitude(),
                                1);

                        Address address = addresses.get(0);

                        String addressLine = address.getAddressLine(0);

                        String district = addressLine.split(", ")[1];

                        if (Arrays.asList(Craft.stores).contains(district)) {
                            binding.inputStore.setText(district);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            LocationRequest locationRequest = LocationRequest.create()
                    .setMaxWaitTime(5000)
                    .setInterval(10000)
                    .setFastestInterval(5000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    public void startTakePhoto(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        mainActivityResultLauncher.launch(intent);
    }
}