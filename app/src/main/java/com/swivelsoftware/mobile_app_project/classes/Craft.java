package com.swivelsoftware.mobile_app_project.classes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.swivelsoftware.mobile_app_project.EditCraftActivity;
import com.swivelsoftware.mobile_app_project.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Craft {
    final static public String addMode = "add";
    final static public String editMode = "edit";

    final static public int BOOKMARK_CODE = 100;
    final static public int HOME_CODE = 101;

    final static public int UNBOOKMARK = 102;
    final static public int BOOKMARK = 103;

    public String id, name, store, date, description, addBy, addTimestamp, photo;

    public Craft(String id, String name, String store, String date, String description, String photo) {
        this.id = id;
        this.name = name;
        this.store = store;
        this.date = date;
        this.description = description;
        this.photo = photo;
    }

    public Craft(String name, String store, String date, String description, String photo) {
        this.name = name;
        this.store = store;
        this.date = date;
        this.description = description;
        this.photo = photo;
    }

    public Craft(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getString("_id");
            this.name = jsonObject.getString("name");
            this.store = jsonObject.getString("store");
            this.description = jsonObject.getString("description");
            this.date = jsonObject.getString("date");
            this.photo = jsonObject.getString("photo");
            this.addBy = jsonObject.getString("addBy");
            this.addTimestamp = jsonObject.getString("addTimestamp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJSONObject(String mode) {
        JSONObject jsonObject = new JSONObject();

        try {
            if (editMode.equals(mode)) {
                jsonObject.put("craftId", id);
            }

            jsonObject.put("name", name);
            jsonObject.put("store", store);
            jsonObject.put("date", date);
            jsonObject.put("description", description);
            jsonObject.put("photo", photo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public static void setCraftCard(Context context, int code, LinearLayout craftCardLayout, View craftCardView) {
        Auth auth = new Auth(context);

        String role = auth.getUserString(Auth.roleKey);

        craftCardLayout.removeAllViews();

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                Request.Method.POST,
                Utils.getBaseUrl(context) + "/dog",
                null,
                response -> {
                    if (response != null) {
                        auth.queryUser(result -> {
                            ArrayList<String> bookmarkList = new ArrayList<>();

                            if (result.has("bookmarks")) {
                                try {
                                    JSONArray bookmarks = result.getJSONArray("bookmarks");

                                    for (int i = 0; i < bookmarks.length(); i++) {
                                        bookmarkList.add(bookmarks.getString(i));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject jsonObject = response.getJSONObject(i);

                                    Craft craft = new Craft(jsonObject);

                                    ImageView photoView = craftCardView.findViewById(R.id.craft_card_photo);

                                    TextView titleView = craftCardView.findViewById(R.id.craft_card_title);
                                    TextView contentView = craftCardView.findViewById(R.id.craft_card_content);

                                    MaterialButton booking = craftCardView.findViewById(R.id.booking);
                                    MaterialButton edit = craftCardView.findViewById(R.id.edit);
                                    MaterialButton delete = craftCardView.findViewById(R.id.delete);
                                    MaterialButton message = craftCardView.findViewById(R.id.message);
                                    MaterialButton bookmark = craftCardView.findViewById(R.id.bookmark);

                                    edit.setOnClickListener(v -> {
                                        Intent intent = new Intent(context, EditCraftActivity.class);
                                        intent.putExtra("mode", Craft.editMode);
                                        intent.putExtra("craftJson", jsonObject.toString());

                                        context.startActivity(intent);
                                    });

                                    delete.setOnClickListener(v -> deleteCraft(context, craft.id, auth, code, craftCardLayout, craftCardView));

                                    if (bookmarkList.contains(craft.id)) {
                                        bookmark.setOnClickListener(v -> bookmarkCraft(context, (MaterialButton) v, craft.id, auth, UNBOOKMARK));
                                        bookmark.setIcon(AppCompatResources.getDrawable(context, R.drawable.ic_baseline_bookmark_added_24));
                                    } else {
                                        bookmark.setOnClickListener(v -> bookmarkCraft(context, (MaterialButton) v, craft.id, auth, BOOKMARK));
                                        bookmark.setIcon(AppCompatResources.getDrawable(context, R.drawable.ic_baseline_bookmark_24));
                                    }

                                    if (!craft.photo.isEmpty()) {
                                        byte[] decodedString = Base64.decode(craft.photo, Base64.DEFAULT);
                                        Bitmap photo = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                        photoView.setImageBitmap(photo);
                                    }

                                    String content = String.format("%s: %s\n" +
                                                    "%s: %s\n" +
                                                    "%s: %s\n" +
                                                    "%s: %s\n" +
                                                    "%s\n",
                                            context.getString(R.string.store), craft.store,
                                            context.getString(R.string.description), craft.description,
                                            context.getString(R.string.date), craft.date,
                                            context.getString(R.string.addBy), craft.addBy,
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

                                craftCardLayout.addView(craftCardView);
                            }
                        });
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
                }
        );

        queue.add(jsonObjectRequest);
    }

    private static void deleteCraft(Context context, String id, Auth auth, int code, LinearLayout craftCardLayout, View craftCardView) {
        JSONObject requestJson = new JSONObject();

        try {
            requestJson.put("craftId", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                Utils.getBaseUrl(context) + "/dog/delete",
                requestJson,
                response -> {
                    if (response != null) {
                        Craft.setCraftCard(context, code, craftCardLayout, craftCardView);

                        Toast.makeText(context, "Deleted", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("authorization", String.format("Bearer %s", auth.getUserString(Auth.authTokenKey)));
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    private static void bookmarkCraft(Context context, MaterialButton bookmarkButton, String id, Auth auth, int action) {
        JSONObject requestJson = new JSONObject();

        try {
            requestJson.put("craftId", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String apiMode = "";
        Drawable bookmarkIcon = null;

        switch (action) {
            case BOOKMARK:
                apiMode = "bookmark";
                bookmarkIcon = AppCompatResources.getDrawable(context, R.drawable.ic_baseline_bookmark_added_24);
                break;
            case UNBOOKMARK:
                apiMode = "unbookmark";
                bookmarkIcon = AppCompatResources.getDrawable(context, R.drawable.ic_baseline_bookmark_add_24);
                break;
        }

        if (!apiMode.isEmpty()) {
            RequestQueue queue = Volley.newRequestQueue(context);

            Drawable finalBookmarkIcon = bookmarkIcon;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    Utils.getBaseUrl(context) + "/user/" + apiMode,
                    requestJson,
                    response -> {
                        if (response != null) {
                            bookmarkButton.setOnClickListener(v -> bookmarkCraft(context, (MaterialButton) v, id, auth, action == BOOKMARK ? UNBOOKMARK : BOOKMARK));
                            bookmarkButton.setIcon(finalBookmarkIcon);
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("authorization", String.format("Bearer %s", auth.getUserString(Auth.authTokenKey)));
                    return headers;
                }
            };

            queue.add(jsonObjectRequest);
        }
    }
}
