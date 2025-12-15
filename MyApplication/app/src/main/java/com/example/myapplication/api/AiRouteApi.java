package com.example.myapplication.api;

import android.os.Handler;
import android.os.Looper;

import com.example.myapplication.model.AIRoute;
import com.example.myapplication.model.Place;
import com.example.myapplication.model.TravelPlan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiRouteApi {

    private static final String BASE_URL = "http://192.168.1.8:8080/api/ai/routes";
    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface AiRouteCallback {
        void onSuccess(List<AIRoute> routes);
        void onError(Throwable t);
    }

    public void getSuggestedRoutes(String bearerToken,
                                   TravelPlan plan,
                                   AiRouteCallback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("travelDate", plan.getTravelDate());
            json.put("durationDays", plan.getDurationDays());

            JSONArray interestsArr = new JSONArray();
            if (plan.getInterests() != null) {
                for (String it : plan.getInterests()) {
                    interestsArr.put(it);
                }
            }
            json.put("interests", interestsArr);

            if (plan.getBudget() != null) {
                json.put("budget", plan.getBudget());
            }
        } catch (JSONException e) {
            callback.onError(e);
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);

        Request.Builder builder = new Request.Builder()
                .url(BASE_URL)
                .post(body);

        if (bearerToken != null && !bearerToken.isEmpty()) {
            builder.addHeader("Authorization", bearerToken);
        }

        Request request = builder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    mainHandler.post(() ->
                            callback.onError(new IOException("HTTP " + response.code()))
                    );
                    return;
                }

                String responseBody = response.body().string();
                try {
                    JSONArray arr = new JSONArray(responseBody);
                    List<AIRoute> routes = parseRoutes(arr);
                    // Đưa kết quả về Main thread để update UI
                    mainHandler.post(() -> callback.onSuccess(routes));
                } catch (JSONException e) {
                    mainHandler.post(() -> callback.onError(e));
                }
            }
        });
    }

    private List<AIRoute> parseRoutes(JSONArray arr) throws JSONException {
        List<AIRoute> result = new ArrayList<>();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);

            String title = obj.optString("title");
            String description = obj.optString("description");
            double distanceKm = obj.optDouble("distanceKm", 0.0);
            String duration = obj.optString("duration");

            List<Place> stops = new ArrayList<>();
            JSONArray stopsArray = obj.optJSONArray("stops");
            if (stopsArray != null) {
                for (int j = 0; j < stopsArray.length(); j++) {
                    JSONObject stopObj = stopsArray.getJSONObject(j);

                    String stopId      = stopObj.optString("id");
                    String stopName    = stopObj.optString("name");
                    String stopDesc    = stopObj.optString("description");
                    String pictureUrl  = stopObj.optString("defaultPicture");
                    String address     = stopObj.optString("address");
                    double latitude    = stopObj.optDouble("latitude");
                    double longitude   = stopObj.optDouble("longitude");

                    Place place = new Place(stopName, stopDesc, "", pictureUrl, address);
                    place.setId(stopId);
                    place.setLatitude(latitude);
                    place.setLongitude(longitude);

                    stops.add(place);
                }
            }

            AIRoute route = new AIRoute(title, description, distanceKm, duration, stops);
            result.add(route);
        }

        return result;
    }
}
