package com.swivelsoftware.mobile_app_project.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.swivelsoftware.mobile_app_project.R;
import com.swivelsoftware.mobile_app_project.classes.Auth;
import com.swivelsoftware.mobile_app_project.classes.Craft;
import com.swivelsoftware.mobile_app_project.classes.Utils;
import com.swivelsoftware.mobile_app_project.databinding.FragmentAdminBinding;
import com.swivelsoftware.mobile_app_project.ui.home.HomeFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

public class AdminFragment extends Fragment {
    private FragmentAdminBinding binding;

    View root;

    Auth auth;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminBinding.inflate(inflater, container, false);

        root = binding.getRoot();

        auth = new Auth(root.getContext());

        Executor executor = ContextCompat.getMainExecutor(root.getContext());
        BiometricPrompt biometricPrompt = new BiometricPrompt(Objects.requireNonNull(getActivity()),
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);

                returnHome();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                getUsers(inflater);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();

                Toast.makeText(root.getContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                returnHome();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Accessing user profile")
                .setSubtitle("Authenticate using your biometric credential")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);

        return root;
    }

    private void returnHome() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);

        transaction.replace(R.id.nav_host_fragment_content_main, HomeFragment.class, null);
        transaction.commit();
    }

    private void getUsers(LayoutInflater inflater) {
        RequestQueue queue = Volley.newRequestQueue(root.getContext());

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                Request.Method.POST,
                String.format("%s/user/all", Utils.getBaseUrl(root.getContext())),
                null,
                response -> {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject userObject = response.getJSONObject(i);
                            Auth user = new Auth(userObject);

                            ArrayList<String> bookmarks = new ArrayList<>();

                            for (i = 0; i < user.bookmarks.length(); i++) {
                                String craftId = user.bookmarks.getString(i);

                                Craft.queryCraftById(root.getContext(),
                                        craftJsonObject -> {
                                            try {
                                                View userCardView = inflater.inflate(R.layout.user_card, binding.adminLinearLayout, false);

                                                TextView firstNameView = userCardView.findViewById(R.id.user_first_name);
                                                TextView lastNameView = userCardView.findViewById(R.id.user_last_name);
                                                TextView emailView = userCardView.findViewById(R.id.user_email);
                                                TextView adminView = userCardView.findViewById(R.id.user_admin);
                                                TextView roleView = userCardView.findViewById(R.id.user_role);
                                                TextView bookmarksView = userCardView.findViewById(R.id.user_bookmarks);

                                                firstNameView.setText(user.firstName);
                                                lastNameView.setText(user.lastName);
                                                emailView.setText(user.email);
                                                adminView.setText(user.admin ? "True" : "False");
                                                roleView.setText(user.role);

                                                String craftName = craftJsonObject.getString("name");

                                                bookmarks.add(String.format("%s (%s)", craftName, craftId));

                                                bookmarksView.setText(String.join("\r\n", bookmarks));

                                                binding.adminLinearLayout.addView(userCardView);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        },
                                        craftId);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(root.getContext(), e.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(root.getContext(), error.toString(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("authorization", String.format("Bearer %s", auth.getUserString(Auth.AUTH_TOKEN_KEY)))
                ;
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}