package com.swivelsoftware.mobile_app_project.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.swivelsoftware.mobile_app_project.R;
import com.swivelsoftware.mobile_app_project.classes.Auth;
import com.swivelsoftware.mobile_app_project.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    Auth auth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        auth = new Auth(root.getContext());
        String role = auth.getUserString(auth.roleKey);

        LinearLayout craft_card_layout = binding.craftCardLayout;

        for (int i = 0; i < 10; i++) {
            View to_add = inflater.inflate(R.layout.craft_card, craft_card_layout, false);

            MaterialButton booking = (MaterialButton) to_add.findViewById(R.id.booking);
            MaterialButton edit = (MaterialButton) to_add.findViewById(R.id.edit);
            MaterialButton delete = (MaterialButton) to_add.findViewById(R.id.delete);
            MaterialButton message = (MaterialButton) to_add.findViewById(R.id.message);
            MaterialButton bookmark = (MaterialButton) to_add.findViewById(R.id.bookmark);

            if (role.equals(auth.roleEmployee)) {
                booking.setVisibility(View.GONE);
//                message.setVisibility(View.GONE);
            } else if (role.equals(auth.rolePublic)) {
//                edit.setVisibility(View.GONE);
//                delete.setVisibility(View.GONE);
            }

            craft_card_layout.addView(to_add);
        }

//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}