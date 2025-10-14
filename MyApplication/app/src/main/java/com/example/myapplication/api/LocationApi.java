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

public class LocationApi {
    private static final String LOCATION_URL = "http://192.168.100.135:8080/api/locations";
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

    public interface LocationApiCallback {
        void onSuccess(ArrayList<JSONObject> locationList);
        void onFailure(String errorMessage);
    }
}
