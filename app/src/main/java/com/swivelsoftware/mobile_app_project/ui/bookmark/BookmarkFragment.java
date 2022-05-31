package com.swivelsoftware.mobile_app_project.ui.bookmark;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.swivelsoftware.mobile_app_project.R;
import com.swivelsoftware.mobile_app_project.classes.Craft;
import com.swivelsoftware.mobile_app_project.databinding.FragmentHomeBinding;

public class BookmarkFragment extends Fragment {
    private FragmentHomeBinding binding;

    LayoutInflater _inflater;

    View craftCardView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _inflater = inflater;

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        craftCardView = _inflater.inflate(R.layout.craft_card, binding.craftCardLayout, false);

        Craft.setCraftCard(root.getContext(), Craft.BOOKMARK_CODE, binding.craftCardLayout, craftCardView);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}