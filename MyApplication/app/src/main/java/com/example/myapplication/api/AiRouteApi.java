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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiRouteApi {
    private static final String BASE_URL = "http://192.168.1.12/api/ai/routes";

    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AiRouteApi() {
        client = new OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    public interface AiRouteCallback {
        void onSuccess(List<AIRoute> routes);
        void onError(Throwable t);
    }
    public void getSuggestedRoutes(
            String bearerToken,
            TravelPlan plan,
            double userLat,
            double userLng,
            AiRouteCallback callback
    ) {
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

            json.put("userLat", userLat);
            json.put("userLng", userLng);

        } catch (JSONException e) {
            callback.onError(e);
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);

        Request.Builder builder = new Request.Builder()
                .url(BASE_URL)
                .post(body);

        if (bearerToken != null && !bearerToken.trim().isEmpty()) {
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
                            callback.onError(new IOException("HTTP " + response.code() + " " + response.message()))
                    );
                    return;
                }

                String responseBody = (response.body() != null) ? response.body().string() : "";
                try {
                    JSONArray arr = new JSONArray(responseBody);
                    List<AIRoute> routes = parseRoutes(arr);
                    mainHandler.post(() -> callback.onSuccess(routes));
                } catch (JSONException e) {
                    mainHandler.post(() -> callback.onError(e));
                }
            }
        });
    }
    public void getSuggestedRoutes(
            String bearerToken,
            TravelPlan plan,
            AiRouteCallback callback
    ) {
        getSuggestedRoutes(bearerToken, plan, 0.0, 0.0, callback);
    }

    private List<AIRoute> parseRoutes(JSONArray arr) throws JSONException {
        List<AIRoute> result = new ArrayList<>();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);

            String title = obj.optString("title", "");
            String description = obj.optString("description", "");
            double distanceKm = obj.optDouble("distanceKm", 0.0);
            String duration = obj.optString("duration", "");

            List<Place> stops = new ArrayList<>();
            JSONArray stopsArr = obj.optJSONArray("stops");
            if (stopsArr != null) {
                for (int j = 0; j < stopsArr.length(); j++) {
                    JSONObject s = stopsArr.optJSONObject(j);
                    if (s == null) continue;

                    String id = s.optString("id", "");
                    String name = s.optString("name", "");
                    String address = s.optString("address", "");
                    String desc = s.optString("description", "");
                    String pic = s.optString("defaultPicture", "");

                    Place place = new Place(
                            name,
                            desc,
                            "",
                            pic,
                            address
                    );

                    if (id != null && !id.trim().isEmpty()) {
                        place.setId(id);
                    }

                    stops.add(place);
                }
            }

            AIRoute route = new AIRoute(title, description, distanceKm, duration, stops);
            result.add(route);
        }

        return result;
    }
}
