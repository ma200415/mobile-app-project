package com.swivelsoftware.mobile_app_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.swivelsoftware.mobile_app_project.databinding.ActivityEditCraftBinding;

import java.util.Calendar;

public class EditCraftActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityEditCraftBinding binding = ActivityEditCraftBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String[] items = new String[]{"Option 1", "Option 2", "Option 3", "Option 4"};
        ArrayAdapter sss = new ArrayAdapter(this, R.layout.craft_store_list_item, items);

        AutoCompleteTextView sssss = (AutoCompleteTextView) binding.store.getEditText();

        sssss.setAdapter(sss);
    }

    public void showDatePicker(View view) {
        MaterialDatePicker dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Select dates")
                        .build();

        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            Pair result = (Pair) selection;
//                fromCal.setTimeInMillis((long)result.first);
//                item.setStartDate(fromCal);
//                toCal.setTimeInMillis((long)result.second);
//                item.setEndDate(toCal);

            Calendar fromCal = Calendar.getInstance();
            Calendar toCal = Calendar.getInstance();

            fromCal.setTimeInMillis((long) result.first);
            toCal.setTimeInMillis((long) result.second);

            Log.d("+++++", fromCal.getTime() + "");
            Log.d("+++++", toCal.getTime() + "");
        });

        dateRangePicker.show(getSupportFragmentManager(), "dateRangePicker");


    }
}