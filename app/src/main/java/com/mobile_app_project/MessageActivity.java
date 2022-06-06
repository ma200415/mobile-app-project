package com.mobile_app_project;

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
import com.mobile_app_project.classes.Auth;
import com.mobile_app_project.classes.Message;
import com.mobile_app_project.classes.Reply;
import com.mobile_app_project.classes.Utils;
import com.mobile_app_project.databinding.ActivityMessageBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessageActivity extends AppCompatActivity {
    ActivityMessageBinding binding;

    Auth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = new Auth(this);

        Intent intent = getIntent();

        if (intent.hasExtra(Message.MESSAGE_EXTRA)) {
            String messageJObjectString = intent.getStringExtra(Message.MESSAGE_EXTRA);

            Message message = null;

            try {
                JSONObject messageJObj = new JSONObject(messageJObjectString);

                message = new Message(messageJObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (message != null) {
                setMessages(message);
            }

            new Handler().post(this::scrollToDown);

            Message finalMessage = message;
            binding.replyLayout.setEndIconOnClickListener(event -> submit(finalMessage));
        } else if (intent.hasExtra(Message.NEW_MESSAGE_EXTRA)) {
            String craftId = intent.getStringExtra(Message.NEW_MESSAGE_EXTRA);

            binding.replyLayout.setEndIconOnClickListener(event -> submitNew(craftId));
        }
    }

    private void scrollToDown() {
        binding.messageScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    private void setMessages(Message message) {
        setMessage(message.userId, message.message, message.timestamp);

        for (int i = 0; i < message.replys.length(); i++) {
            Reply reply = null;

            try {
                reply = new Reply(message.replys.getJSONObject(i));
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

    private void submit(Message message) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("message", Objects.requireNonNull(binding.reply.getText()).toString());
            jsonObject.put("messageId", message.messageId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        send(jsonObject);
    }

    private void submitNew(String craftId) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("message", Objects.requireNonNull(binding.reply.getText()).toString());
            jsonObject.put("craftId", craftId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        send(jsonObject);
    }

    private void send(JSONObject jsonObject) {
        if (Objects.requireNonNull(binding.reply.getText()).toString().trim().isEmpty()) {
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                String.format("%s/message/addnew", Utils.getBaseUrl(this)),
                jsonObject,
                response -> {
                    try {
                        if (response.has("_id")) {
                            Message message = new Message(response);

                            binding.replyLayout.setEndIconOnClickListener(event -> submit(message));
                        }

                        if (response.has("_id") || (response.has("success") && response.getBoolean("success"))) {
                            Reply reply = new Reply(response);

                            setMessage(reply.userId, reply.message, reply.timestamp);

                            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                            binding.reply.setText(null);

                            scrollToDown();
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