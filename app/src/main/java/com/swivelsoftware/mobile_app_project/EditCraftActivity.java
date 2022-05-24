package com.swivelsoftware.mobile_app_project;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.swivelsoftware.mobile_app_project.databinding.ActivityEditCraftBinding;

import java.util.Calendar;

public class EditCraftActivity extends AppCompatActivity {
    ActivityEditCraftBinding binding;

    final String[] stores = new String[]{"Wong Tai Sin", "Tsuen Wan", "Causeway Bay", "Mong Kok"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditCraftBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArrayAdapter sss = new ArrayAdapter(this, R.layout.craft_store_list_item, stores);

        AutoCompleteTextView sssss = (AutoCompleteTextView) binding.store.getEditText();

        sssss.setAdapter(sss);

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

            Log.d("+++++", fromCal.getTime() + "");
            Log.d("+++++", toCal.getTime() + "");

            binding.date.getEditText().setText(fromCal.getTime().toString());
        });

        dateRangePicker.show(getSupportFragmentManager(), "dateRangePicker");
    }
}