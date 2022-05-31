package com.swivelsoftware.mobile_app_project.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.swivelsoftware.mobile_app_project.R;
import com.swivelsoftware.mobile_app_project.classes.Craft;
import com.swivelsoftware.mobile_app_project.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    View root, craftCardView;

    LayoutInflater _inflater;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _inflater = inflater;

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        craftCardView = _inflater.inflate(R.layout.craft_card, binding.craftCardLayout, false);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        Craft.setCraftCard(root.getContext(), Craft.HOME_CODE, binding.craftCardLayout, craftCardView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}