package com.example.myapplication.api;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReviewApi {
    private static final String REVIEW_URL = "http://192.168.1.174:8080/api/reviews";

    public static void GetReviewListByAddress(String address, String sort, Context context, ReviewApi.ReviewApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            HttpUrl url = HttpUrl.parse(REVIEW_URL + "/get-list")
                    .newBuilder()
                    .addQueryParameter("address", address)
                    .addQueryParameter("sortType", sort)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
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

                            ArrayList<JSONObject> reviewList = new ArrayList<>();

                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject reviewObj = resultArray.getJSONObject(i);
                                reviewList.add(reviewObj);
                            }
                            // 🔹 Gọi callback thành công và truyền danh sách checkpoints
                            callback.onSuccess(reviewList);
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

    public static void AddReview(String jwt, String address, int rating, String content, List<String> pictureUrls, Context context, ReviewApi.ReviewApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONArray jsonPictureUrls = new JSONArray();
            if (pictureUrls != null) {
                for (String url : pictureUrls) {
                    jsonPictureUrls.put(url);
                }
            }

            JSONObject json = new JSONObject();
            json.put("locationAddress", address);
            json.put("rating", rating);
            json.put("content", content);
            json.put("pictureUrls", jsonPictureUrls);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            HttpUrl url = HttpUrl.parse(REVIEW_URL + "/add")
                    .newBuilder()
                    .build();

            Request request = new Request.Builder()
                    .url(url)
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
                            JSONObject result = new JSONObject(response.body().string());
                            String msg = result.getString("message");
                            callback.onSuccess(msg);
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

    public static void UpdateReview(String jwt, String address, int rating, String content, List<String> pictureUrls, Context context, ReviewApi.ReviewApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONArray jsonPictureUrls = new JSONArray();
            if (pictureUrls != null) {
                for (String url : pictureUrls) {
                    jsonPictureUrls.put(url);
                }
            }

            JSONObject json = new JSONObject();
            json.put("locationAddress", address);
            json.put("rating", rating);
            json.put("content", content);
            json.put("pictureUrls", jsonPictureUrls);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            HttpUrl url = HttpUrl.parse(REVIEW_URL + "/update")
                    .newBuilder()
                    .build();

            Request request = new Request.Builder()
                    .url(url)
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
                            JSONObject result = new JSONObject(response.body().string());
                            String msg = result.getString("message");
                            callback.onSuccess(msg);
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

    public static void DeleteReview(String jwt, String address, Context context, ReviewApi.ReviewApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            HttpUrl url = HttpUrl.parse(REVIEW_URL + "/delete")
                    .newBuilder()
                    .addQueryParameter("address", address)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
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
                            JSONObject result = new JSONObject(response.body().string());
                            String msg = result.getString("message");
                            callback.onSuccess(msg);
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

    public static void LikeReview(String jwt, String authorName, String address, Context context, ReviewApi.ReviewApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            HttpUrl url = HttpUrl.parse(REVIEW_URL + "/like")
                    .newBuilder()
                    .addQueryParameter("address", address)
                    .addQueryParameter("authorName", authorName)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
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
                            JSONObject result = new JSONObject(response.body().string());
                            String msg = result.getString("message");
                            callback.onSuccess(msg);
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

    public static void GetLikedReviews(String address, String jwt, Context context, ReviewApi.ReviewApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            HttpUrl url = HttpUrl.parse(REVIEW_URL + "/get-liked-reviews")
                    .newBuilder()
                    .addQueryParameter("address", address)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .header("Authorization", "Bearer " + jwt)
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

                            ArrayList<JSONObject> reviewList = new ArrayList<>();

                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject reviewObj = resultArray.getJSONObject(i);
                                reviewList.add(reviewObj);
                            }
                            // 🔹 Gọi callback thành công và truyền danh sách checkpoints
                            callback.onSuccess(reviewList);
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

    public interface ReviewApiCallback {
        void onSuccess(ArrayList<JSONObject> dataList);

        void onSuccess(String msg);
        void onFailure(String errorMessage);
    }
}
