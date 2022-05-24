package com.swivelsoftware.mobile_app_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.android.volley.AuthFailureError;
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

public class EditCraftActivity extends AppCompatActivity {
    ActivityEditCraftBinding binding;

    final String[] stores = new String[]{"Wong Tai Sin", "Tsuen Wan", "Causeway Bay", "Mong Kok"};

    Auth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditCraftBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = new Auth(this);

        ArrayAdapter sss = new ArrayAdapter(this, R.layout.craft_store_list_item, stores);

        AutoCompleteTextView sssss = (AutoCompleteTextView) binding.store.getEditText();

        sssss.setAdapter(sss);

        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");

        switch (mode) {
            case Craft.addMode:
                binding.craftMode.setText(R.string.add);
                break;
            case Craft.editMode:
                binding.craftMode.setText(R.string.edit);
                break;
        }

        binding.date.setEndIconOnClickListener(event -> showDatePicker());
    }

    public void showDatePicker() {
        MaterialDatePicker dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Select dates")
                        .build();

        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            Pair result = (Pair) selection;

            Calendar fromCal = Calendar.getInstance();
            Calendar toCal = Calendar.getInstance();

            fromCal.setTimeInMillis((long) result.first);
            toCal.setTimeInMillis((long) result.second);

            binding.date.getEditText().setText(fromCal.getTime().toString());
        });

        dateRangePicker.show(getSupportFragmentManager(), "dateRangePicker");
    }

    public void submit(View view) {
        String store = binding.inputStore.getText().toString();
        String date = binding.inputDate.getText().toString();
        String remark = binding.inputRemark.getText().toString();

        Craft craft = new Craft(store, date, remark);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                "http://10.0.2.2:3001/dog/add",
                craft.getJSONObject(),
                response -> {
                    try {
                        if (response.has("success") && response.getBoolean("success")) {


//                            Intent intent = new Intent();
//                            intent.putExtra("ACTION", "login");
//                            setResult(RESULT_OK, intent);

                            this.finish();
                        } else {
//                            String message = response.getString("message");

//                            switch (response.getString("errorType")) {
//                                case "email":
//                                    email.setError(message);
//                                    email.requestFocus();
//                                    break;
//                                case "password":
//                                    password.setError(message);
//                                    password.requestFocus();
//                                    break;
//                                default:
//                                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
//                                    break;
//                            }
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