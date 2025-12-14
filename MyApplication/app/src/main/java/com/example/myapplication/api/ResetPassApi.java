package com.example.myapplication.api;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResetPassApi {
    private static final String BASE_URL = "http://192.168.1.174:8080/api/auth";

    public static void forgotPassword(String email, Context context, ResetPassApi.ResetPassApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();
            json.put("email", email);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + "/forgot-password")
                    .post(body)
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
                            String message = respJson.getString("message");
                            callback.onSuccess(message);
                        } catch (JSONException e) {
                            callback.onFailure("Failed to parse response");
                        }
                    } else {
                        callback.onFailure("Error: " + response.code() + " " + response.message());
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }

    public static void verifyEmail(String email, String otp, Context context, ResetPassApi.ResetPassApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("otp", otp);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + "/verify-otp")
                    .post(body)
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
                            String message = respJson.getString("message");
                            callback.onSuccess(message);
                        } catch (JSONException e) {
                            callback.onFailure("Failed to parse response");
                        }
                    } else {
                        callback.onFailure("Error: " + response.code() + " " + response.message());
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }

    public static void resetPassword(String email, String password, Context context, ResetPassApi.ResetPassApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("newPassword", password);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + "/reset-password")
                    .post(body)
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
                            String message = respJson.getString("message");
                            callback.onSuccess(message);
                        } catch (JSONException e) {
                            callback.onFailure("Failed to parse response");
                        }
                    } else {
                        callback.onFailure("Error: " + response.code() + " " + response.message());
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }

    public interface ResetPassApiCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }
}
