package com.swivelsoftware.mobile_app_project;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.swivelsoftware.mobile_app_project.classes.Auth;
import com.swivelsoftware.mobile_app_project.classes.Message;
import com.swivelsoftware.mobile_app_project.classes.Reply;
import com.swivelsoftware.mobile_app_project.classes.Utils;
import com.swivelsoftware.mobile_app_project.databinding.ActivityMessageBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessageActivity extends AppCompatActivity {
    ActivityMessageBinding binding;

    Auth auth;

    Message _message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = new Auth(this);

        Intent intent = getIntent();
        String messageJObjectString = intent.getStringExtra("messageJObjectString");

        try {
            JSONObject messageJObj = new JSONObject(messageJObjectString);

            _message = new Message(messageJObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setMessages();

        new Handler().post(() -> binding.messageScrollView.fullScroll(ScrollView.FOCUS_DOWN));

        binding.replyLayout.setEndIconOnClickListener(event -> submit());
    }

    private void setMessages() {
        setMessage(_message.userId, _message.message, _message.timestamp);

        for (int i = 0; i < _message.replys.length(); i++) {
            Reply reply = null;

            try {
                reply = new Reply(_message.replys.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (reply != null) {
                setMessage(reply.userId, reply.message, reply.timestamp);
            }
        }
    }

    private void setMessage(String userId, String message, String timestamp) {
        LinearLayoutCompat messageCard = (LinearLayoutCompat) getLayoutInflater().inflate(R.layout.message_card, binding.messagesLayout, false);

        TextView messageView = messageCard.findViewById(R.id.message);
        TextView tsView = messageCard.findViewById(R.id.timestamp);

        messageView.setText(message);

        tsView.setText(Utils.formatDateTime(timestamp));

        if (auth.getUserString(Auth.USERID_KEY).equals(userId)) {
            messageCard.setGravity(Gravity.END);
        }

        binding.messagesLayout.addView(messageCard);
    }

    private void submit() {
        if (Objects.requireNonNull(binding.reply.getText()).toString().trim().isEmpty()) {
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("message", Objects.requireNonNull(binding.reply.getText()).toString());
            jsonObject.put("messageId", _message.messageId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                String.format("%s/message/addnew", Utils.getBaseUrl(this)),
                jsonObject,
                response -> {
                    try {
                        if (response.has("success") && response.getBoolean("success")) {
                            Reply reply = new Reply(response);

                            setMessage(reply.userId, reply.message, reply.timestamp);

                            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                            binding.reply.setText(null);

                            binding.messageScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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
                headers.put("authorization", String.format("Bearer %s", auth.getUserString(Auth.AUTH_TOKEN_KEY)));
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }
}