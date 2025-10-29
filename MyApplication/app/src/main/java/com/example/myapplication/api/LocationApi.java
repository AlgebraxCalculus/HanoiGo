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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LocationApi {
    private static final String LOCATION_URL = "http://192.168.50.50:8080/api/locations";
    public static void GetLocationList(double lat, double lng, String tag, boolean topVisited, boolean popularNearU, Context context, LocationApi.LocationApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            HttpUrl url = HttpUrl.parse(LOCATION_URL + "/get-list")
                    .newBuilder()
                    .addQueryParameter("lat", String.valueOf(lat))
                    .addQueryParameter("lng", String.valueOf(lng))
                    .addQueryParameter("tag", tag)
                    .addQueryParameter("mostVisited", String.valueOf(topVisited))
                    .addQueryParameter("nearest", String.valueOf(popularNearU))
                    .addQueryParameter("limit", String.valueOf(10))
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            System.out.println("URL: "+url);
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            ArrayList<JSONObject> locationList = new ArrayList<>();
                            JSONArray respJson = new JSONObject(response.body().string()).getJSONArray("result");
                            for (int i = 0; i < respJson.length(); i++) {
                                locationList.add(respJson.getJSONObject(i));
                            }
                            callback.onSuccess(locationList);
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

    public static void GetLocationByDetail(String address, Context context, LocationDetailCallback callback) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = HttpUrl.parse(LOCATION_URL + "/get-detail-by-address")
                .newBuilder()
                .addQueryParameter("address", address)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        System.out.println("DETAIL URL: " + url);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONObject result = json.getJSONObject("result");
                        callback.onSuccess(result);
                    } catch (JSONException e) {
                        callback.onFailure("Failed to parse detail");
                    }
                } else {
                    callback.onFailure("Error: " + response.code());
                }
            }
        });
    }

    public static void SearchAutocomplete(String keyword, Context context, LocationApiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = HttpUrl.parse(LOCATION_URL + "/search-autocomplete")
                .newBuilder()
                .addQueryParameter("keyword", keyword)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        System.out.println("AUTOCOMPLETE URL: " + url);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        ArrayList<JSONObject> resultList = new ArrayList<>();
                        JSONArray respJson = new JSONObject(response.body().string()).getJSONArray("result");
                        for (int i = 0; i < respJson.length(); i++) {
                            resultList.add(respJson.getJSONObject(i));
                        }
                        callback.onSuccess(resultList);
                    } catch (JSONException e) {
                        callback.onFailure("Failed to parse autocomplete result");
                    }
                } else {
                    callback.onFailure("Error: " + response.code());
                }
            }
        });
    }


    // Callback interface
    public interface LocationApiCallback {
        void onSuccess(ArrayList<JSONObject> locationList);
        void onFailure(String errorMessage);
    }

    public interface LocationDetailCallback {
        void onSuccess(JSONObject locationDetail);
        void onFailure(String errorMessage);
    }
}
