package com.swivelsoftware.mobile_app_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

    final String[] stores = new String[]{"Wong Tai Sin", "Tsuen Wan", "Causeway Bay", "Mong Kok"};

    Auth auth;
    Craft craft;

    String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditCraftBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = new Auth(this);

        ArrayAdapter<String> storeAdapter = new ArrayAdapter<>(this, R.layout.craft_store_list_item, stores);

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
                String.format("http://10.0.2.2:3001/dog/%s", mode),
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
}