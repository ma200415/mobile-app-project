package com.swivelsoftware.mobile_app_project.ui.message;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.swivelsoftware.mobile_app_project.MessageActivity;
import com.swivelsoftware.mobile_app_project.R;
import com.swivelsoftware.mobile_app_project.classes.Auth;
import com.swivelsoftware.mobile_app_project.classes.Craft;
import com.swivelsoftware.mobile_app_project.classes.Message;
import com.swivelsoftware.mobile_app_project.classes.Reply;
import com.swivelsoftware.mobile_app_project.classes.Utils;
import com.swivelsoftware.mobile_app_project.databinding.FragmentMessageBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MessageFragment extends Fragment {
    private FragmentMessageBinding binding;

    LayoutInflater _inflater;

    View root;

    Auth auth;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMessageBinding.inflate(inflater, container, false);

        root = binding.getRoot();

        auth = new Auth(root.getContext());

        _inflater = inflater;

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        getMessages();
    }

    private void getMessages() {
        RequestQueue queue = Volley.newRequestQueue(root.getContext());

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                Request.Method.GET,
                String.format("%s/message", Utils.getBaseUrl(root.getContext())),
                null,
                response -> {
                    if (response != null && response.length() > 0) {
                        binding.messagesLayout.removeAllViews();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject messageJObj = null;

                            Message message = null;

                            String messageString = null;
                            String timestamp = null;

                            try {
                                messageJObj = response.getJSONObject(i);

                                message = new Message(messageJObj);

                                if (message.replys != null && message.replys.length() > 0) {
                                    Reply lastReply = new Reply(message.replys.getJSONObject(message.replys.length() - 1));

                                    messageString = lastReply.message;
                                    timestamp = lastReply.timestamp;
                                } else {
                                    messageString = message.message;
                                    timestamp = message.timestamp;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            View messageCardView = _inflater.inflate(R.layout.message_list_card, binding.messagesLayout, false);

                            JSONObject finalMessageJObj = messageJObj;

                            messageCardView.setOnClickListener(e -> {
                                Intent intent = new Intent(root.getContext(), MessageActivity.class);
                                intent.putExtra(Message.MESSAGE_EXTRA, finalMessageJObj.toString());

                                startActivity(intent);
                            });

                            TextView lastMessageView = messageCardView.findViewById(R.id.message_list_last_message);
                            TextView userView = messageCardView.findViewById(R.id.message_list_user);
                            TextView timestampView = messageCardView.findViewById(R.id.message_list_timestamp);

                            lastMessageView.setText(messageString);
                            timestampView.setText(Utils.formatDateTime(timestamp));

                            auth.queryUserById(user -> {
                                        Auth _auth = new Auth(user);

                                        userView.setText(String.format("%s %s", _auth.firstName, _auth.lastName));
                                    },
                                    message.userId);

                            Craft.queryCraftById(root.getContext(),
                                    craftJsonObject -> {
                                        String craftName = null;

                                        try {
                                            craftName = craftJsonObject.getString("name");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        TextView craftNameView = messageCardView.findViewById(R.id.message_list_craft_name);

                                        craftNameView.setText(craftName);
                                    },
                                    message.craftId);

                            binding.messagesLayout.addView(messageCardView);
                        }
                    } else {
                        View noResultsView = _inflater.inflate(R.layout.no_result, binding.messagesLayout, false);

                        binding.messagesLayout.addView(noResultsView);
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
                headers.put("authorization", String.format("Bearer %s", auth.getUserString(Auth.AUTH_TOKEN_KEY)));
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