package com.swivelsoftware.mobile_app_project.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.swivelsoftware.mobile_app_project.R;
import com.swivelsoftware.mobile_app_project.classes.Auth;
import com.swivelsoftware.mobile_app_project.databinding.FragmentHomeBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    Auth auth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        auth = new Auth(root.getContext());
        String role = auth.getUserString(Auth.roleKey);

        LinearLayout craft_card_layout = binding.craftCardLayout;

        RequestQueue queue = Volley.newRequestQueue(root.getContext());

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                Request.Method.POST,
                "http://10.0.2.2:3001/dog",
                null,
                response -> {
                    if (response != null) {

                        for (int i = 0; i < response.length(); i++) {
                            View to_add = inflater.inflate(R.layout.craft_card, craft_card_layout, false);

                            try {
                                JSONObject jsonObject = response.getJSONObject(i);

                                String store = jsonObject.getString("store");
                                String remark = jsonObject.getString("remark");
                                String date = jsonObject.getString("date");
                                String addBy = jsonObject.getString("addBy");
                                String addTimestamp = jsonObject.getString("addTimestamp");

                                MaterialButton booking = to_add.findViewById(R.id.booking);
                                MaterialButton edit = to_add.findViewById(R.id.edit);
                                MaterialButton delete = to_add.findViewById(R.id.delete);
                                MaterialButton message = to_add.findViewById(R.id.message);
                                MaterialButton bookmark = to_add.findViewById(R.id.bookmark);

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

                            craft_card_layout.addView(to_add);
                        }
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(root.getContext(), error.toString(), Toast.LENGTH_LONG).show();
                }
        );

        queue.add(jsonObjectRequest);

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