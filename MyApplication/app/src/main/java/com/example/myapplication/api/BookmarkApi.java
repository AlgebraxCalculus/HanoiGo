package com.example.myapplication.api;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BookmarkApi {
    private static final String BOOKMARK_LIST_URL = "http://192.168.1.174:8080/api/bookmark-lists";
    private static final String BOOKMARK_URL = "http://192.168.1.174:8080/api/bookmarks";

    /**
     * Lấy tất cả bookmark lists của user
     * GET /api/bookmark-lists/my-lists
     */
    public static void getMyBookmarkLists(String jwt, Context context, BookmarkListCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(BOOKMARK_LIST_URL + "/my-lists")
                .get()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        JSONArray resultArray = jsonResponse.getJSONArray("result");

                        ArrayList<JSONObject> listData = new ArrayList<>();
                        for (int i = 0; i < resultArray.length(); i++) {
                            listData.add(resultArray.getJSONObject(i));
                        }
                        callback.onSuccess(listData);
                    } catch (JSONException e) {
                        callback.onFailure("Failed to parse response");
                    }
                } else {
                    callback.onFailure("Error: " + response.code());
                }
            }
        });
    }

    /**
     * Tạo bookmark list mới
     * POST /api/bookmark-lists/create
     */
    public static void createBookmarkList(String jwt, String name, String icon, Context context, SingleBookmarkListCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("icon", icon);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BOOKMARK_LIST_URL + "/create")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + jwt)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response.body().string());
                            JSONObject result = jsonResponse.getJSONObject("result");
                            callback.onSuccess(result);
                        } catch (JSONException e) {
                            callback.onFailure("Failed to parse response");
                        }
                    } else {
                        callback.onFailure("Error: " + response.code());
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }

    /**
     * Xóa bookmark list
     * DELETE /api/bookmark-lists/{listId}
     */
    public static void deleteBookmarkList(String jwt, String listId, Context context, VoidCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(BOOKMARK_LIST_URL + "/" + listId)
                .delete()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Error: " + response.code());
                }
            }
        });
    }

    /**
     * Lấy tất cả bookmarks trong 1 bookmark list
     * GET /api/bookmarks/list/{listId}
     */
    public static void getBookmarksInList(String jwt, String listId, Context context, BookmarkCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(BOOKMARK_URL + "/list/" + listId)
                .get()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        JSONArray resultArray = jsonResponse.getJSONArray("result");

                        ArrayList<JSONObject> bookmarks = new ArrayList<>();
                        for (int i = 0; i < resultArray.length(); i++) {
                            bookmarks.add(resultArray.getJSONObject(i));
                        }
                        callback.onSuccess(bookmarks);
                    } catch (JSONException e) {
                        callback.onFailure("Failed to parse response");
                    }
                } else {
                    callback.onFailure("Error: " + response.code());
                }
            }
        });
    }

    /**
     * Thêm bookmark vào list
     * POST /api/bookmarks/add
     */
    public static void addBookmark(String jwt, String locationId, String bookmarkListId, String description, Context context, SingleBookmarkCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();
            json.put("locationId", locationId);
            json.put("bookmarkListId", bookmarkListId);
            if (description != null && !description.isEmpty()) {
                json.put("description", description);
            }

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BOOKMARK_URL + "/add")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + jwt)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response.body().string());
                            JSONObject result = jsonResponse.getJSONObject("result");
                            callback.onSuccess(result);
                        } catch (JSONException e) {
                            callback.onFailure("Failed to parse response");
                        }
                    } else {
                        callback.onFailure("Error: " + response.code());
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }

    /**
     * Xóa bookmark khỏi list
     * DELETE /api/bookmarks/remove
     */
    public static void removeBookmark(String jwt, String locationId, String bookmarkListId, Context context, VoidCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();
            json.put("locationId", locationId);
            json.put("bookmarkListId", bookmarkListId);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BOOKMARK_URL + "/remove")
                    .delete(body)
                    .addHeader("Authorization", "Bearer " + jwt)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Error: " + response.code());
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }

    // Callback interfaces
    public interface BookmarkListCallback {
        void onSuccess(ArrayList<JSONObject> bookmarkLists);
        void onFailure(String errorMessage);
    }

    public interface SingleBookmarkListCallback {
        void onSuccess(JSONObject bookmarkList);
        void onFailure(String errorMessage);
    }

    public interface BookmarkCallback {
        void onSuccess(ArrayList<JSONObject> bookmarks);
        void onFailure(String errorMessage);
    }

    public interface SingleBookmarkCallback {
        void onSuccess(JSONObject bookmark);
        void onFailure(String errorMessage);
    }

    public interface VoidCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}
