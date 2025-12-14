package com.example.myapplication.api;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CheckpointApi {
    private static final String CHECKPOINT_URL = "http://192.168.1.174:8080/api/checkpoints";
    public static void GetEnableCheckIn(double lat, double lng, String jwt, Context context, CheckpointApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        // Tạo URL với query parameters
        HttpUrl url = HttpUrl.parse(CHECKPOINT_URL + "/enable-checkin")
                .newBuilder()
                .addQueryParameter("latitude", String.valueOf(lat))
                .addQueryParameter("longitude", String.valueOf(lng))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + jwt)
                .get()
                .build();

        Log.d("CheckpointApi", "ENABLE CHECK-IN URL: " + url);

        // Gửi request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String respString = response.body().string();
                        JSONObject respJson = new JSONObject(respString);

                        if (respJson.has("result")) {
                            JSONArray resultArray = respJson.getJSONArray("result");
                            ArrayList<JSONObject> checkpointList = new ArrayList<>();

                            for (int i = 0; i < resultArray.length(); i++) {
                                checkpointList.add(resultArray.getJSONObject(i));
                            }

                            callback.onSuccess(checkpointList);
                        } else {
                            callback.onFailure("No result field in response");
                        }
                    } catch (JSONException e) {
                        callback.onFailure("Failed to parse JSON: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }

    public static void CheckIn(String jwt, String locationAddress, Context context, CheckpointApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        String url = CHECKPOINT_URL + "/checkin";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("locationAddress", locationAddress);
        } catch (JSONException e) {
            callback.onFailure("Failed to build JSON body: " + e.getMessage());
            return;
        }

        // Tạo RequestBody
        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                jsonBody.toString(),
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        // Tạo Request với Header Bearer Token
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + jwt)
                .post(body)
                .build();

        Log.d("CheckpointApi", "CHECK-IN URL: " + url);
        Log.d("CheckpointApi", "REQUEST BODY: " + jsonBody.toString());

        // Gửi request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String respString = response.body().string();
                        JSONObject respJson = new JSONObject(respString);

                        if (respJson.has("result")) {
                            JSONObject resultObj = respJson.getJSONObject("result");

                            // Trả kết quả về callback dạng ArrayList để dùng chung với GetEnableCheckIn()
                            ArrayList<JSONObject> resultList = new ArrayList<>();
                            resultList.add(resultObj);
                            callback.onSuccess(resultList);

                        } else {
                            callback.onFailure("Missing 'result' in response");
                        }
                    } catch (JSONException e) {
                        callback.onFailure("JSON parse error: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }
    // Callback interface
    public interface CheckpointApiCallback {
        void onSuccess(ArrayList<JSONObject> checkpointList);
        void onFailure(String errorMessage);
    }

}
