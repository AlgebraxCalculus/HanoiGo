package com.example.myapplication.api;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DirectionApi {
    private static final String DIRECTION_URL = "http://192.168.134.5:8080/api/directions";

    public static void GetDirection(double originLat, double originLng, double destLat, double destLng,
                                     Context context, DirectionApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = HttpUrl.parse(DIRECTION_URL)
                .newBuilder()
                .addQueryParameter("originLat", String.valueOf(originLat))
                .addQueryParameter("originLng", String.valueOf(originLng))
                .addQueryParameter("destLat", String.valueOf(destLat))
                .addQueryParameter("destLng", String.valueOf(destLng))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Log.d("DirectionApi", "DIRECTION URL: " + url);

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
                            JSONObject result = respJson.getJSONObject("result");
                            callback.onSuccess(result);
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

    public interface DirectionApiCallback {
        void onSuccess(JSONObject directionResult);
        void onFailure(String errorMessage);
    }
}
