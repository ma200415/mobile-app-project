package com.swivelsoftware.mobile_app_project.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.swivelsoftware.mobile_app_project.EditCraftActivity;
import com.swivelsoftware.mobile_app_project.R;
import com.swivelsoftware.mobile_app_project.classes.Auth;
import com.swivelsoftware.mobile_app_project.classes.Craft;
import com.swivelsoftware.mobile_app_project.databinding.FragmentHomeBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    Auth auth;

    View root;

    LayoutInflater _inflater;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        _inflater = inflater;

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        auth = new Auth(root.getContext());

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
    public void onResume() {
        super.onResume();

        setCraftCard();
    }

    private void setCraftCard() {
        String role = auth.getUserString(Auth.roleKey);

        binding.craftCardLayout.removeAllViews();

        RequestQueue queue = Volley.newRequestQueue(root.getContext());

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                Request.Method.POST,
                root.getContext().getSharedPreferences("APIUrl", MODE_PRIVATE)
                        .getString("apiUrl", "") + "/dog",
                null,
                response -> {
                    if (response != null) {
                        for (int i = 0; i < response.length(); i++) {
                            View craftCardView = _inflater.inflate(R.layout.craft_card, binding.craftCardLayout, false);

                            try {
                                JSONObject jsonObject = response.getJSONObject(i);

                                Craft craft = new Craft(jsonObject);

                                TextView titleView = craftCardView.findViewById(R.id.craft_card_title);
                                TextView contentView = craftCardView.findViewById(R.id.craft_card_content);

                                MaterialButton booking = craftCardView.findViewById(R.id.booking);
                                MaterialButton edit = craftCardView.findViewById(R.id.edit);
                                MaterialButton delete = craftCardView.findViewById(R.id.delete);
                                MaterialButton message = craftCardView.findViewById(R.id.message);
                                MaterialButton bookmark = craftCardView.findViewById(R.id.bookmark);

                                edit.setOnClickListener(l -> {
                                    Intent intent = new Intent(root.getContext(), EditCraftActivity.class);
                                    intent.putExtra("mode", Craft.editMode);
                                    intent.putExtra("craftJson", jsonObject.toString());

                                    startActivity(intent);
                                });

                                String content = String.format("%s: %s\n" +
                                                "%s: %s\n" +
                                                "%s: %s\n" +
                                                "%s: %s\n" +
                                                "%s\n",
                                        getString(R.string.store), craft.store,
                                        getString(R.string.description), craft.description,
                                        getString(R.string.date), craft.date,
                                        getString(R.string.addBy), craft.addBy,
                                        craft.addTimestamp);

                                titleView.setText(craft.name);
                                contentView.setText(content);

                                if (role.equals(Auth.roleEmployee)) {
                                    booking.setVisibility(View.GONE);
//                message.setVisibility(View.GONE);
                                } else if (role.equals(Auth.rolePublic)) {
//                edit.setVisibility(View.GONE);
//                delete.setVisibility(View.GONE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            binding.craftCardLayout.addView(craftCardView);
                        }
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(root.getContext(), error.toString(), Toast.LENGTH_LONG).show();
                }
        );

        queue.add(jsonObjectRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}