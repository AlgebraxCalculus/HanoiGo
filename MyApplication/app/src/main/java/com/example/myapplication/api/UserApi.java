package com.example.myapplication.api;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserApi {
    private static final String USER_URL = "http://192.168.1.3:8080/api/users";
    private static final String ACHIEVEMENT_URL = "http://192.168.1.3:8080/api/achievements";

    public static void getMe(String jwt, Context context, UserApi.UserApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(USER_URL + "/me")
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
                            JSONObject respJson = new JSONObject(response.body().string()).getJSONObject("result");
                            JSONObject userObj = respJson;
                            callback.onSuccess(userObj);
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

    public static void getMyRank(String jwt, Context context, UserApi.UserApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(USER_URL + "/my-rank")
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
                            JSONObject respJson = new JSONObject(response.body().string());
                            JSONObject userObj = respJson;
                            callback.onSuccess(userObj);
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

    public static void GetMyAchievementList(String jwt, String type, String sort, Context context, UserApi.UserApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            HttpUrl url = HttpUrl.parse(ACHIEVEMENT_URL + "/me")
                    .newBuilder()
                    .addQueryParameter(type, sort)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
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

                            ArrayList<JSONObject> achievementList = new ArrayList<>();

                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject achievementObj = resultArray.getJSONObject(i);
                                achievementList.add(achievementObj);
                            }
                            // 🔹 Gọi callback thành công và truyền danh sách achievements
                            callback.onSuccess(achievementList);
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

    public static void GetUserListOrderByPoints(Context context, UserApi.UserApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            HttpUrl url = HttpUrl.parse(USER_URL + "/get")
                    .newBuilder()
                    .addQueryParameter("orderByPoints", "true")
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

                            ArrayList<JSONObject> userList = new ArrayList<>();

                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject userObj = resultArray.getJSONObject(i);
                                userList.add(userObj);
                            }
                            // 🔹 Gọi callback thành công và truyền danh sách achievements
                            callback.onSuccess(userList);
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

    public static void getChartData(String jwt, Context context, UserApi.UserApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            HttpUrl url = HttpUrl.parse(USER_URL + "/get-chartData")
                    .newBuilder()
                    .build();

            Request request = new Request.Builder()
                    .url(url)
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
                            JSONObject jsonResponse = new JSONObject(response.body().string()).getJSONObject("result");
                            JSONArray resultArray = jsonResponse.getJSONArray("data");

                            ArrayList<JSONObject> dataList = new ArrayList<>();

                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject dataObj = resultArray.getJSONObject(i);
                                dataList.add(dataObj);
                            }
                            // 🔹 Gọi callback thành công và truyền danh sách achievements
                            callback.onSuccess(dataList);
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

    public interface UserApiCallback {
        void onSuccess(ArrayList<JSONObject> dataList);
        void onSuccess(JSONObject userObj);
        void onFailure(String errorMessage);
    }
}
