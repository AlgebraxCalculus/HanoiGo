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

    // ✅ Đúng format: baseUrl không nên kèm / cuối nếu bạn tự nối path
    private static final String BASE_URL = "http://10.0.2.2:8080";
    private static final String ROUTE_PATH = "/api/ai/routes";

    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Callback để trả kết quả về Fragment
    public interface AiRouteCallback {
        void onSuccess(List<AIRoute> routes);
        void onError(Throwable t);
    }

    public void getSuggestedRoutes(String bearerToken,
                                   TravelPlan plan,
                                   AiRouteCallback callback) {

        // 1) Build JSON body từ TravelPlan
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

            // budget optional
            if (plan.getBudget() != null) {
                json.put("budget", plan.getBudget());
            }
        } catch (JSONException e) {
            callback.onError(e);
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);

        // 2) Tạo request OkHttp
        Request.Builder builder = new Request.Builder()
                .url(BASE_URL + ROUTE_PATH)
                .post(body)
                .addHeader("Content-Type", "application/json");

        // ✅ Header Authorization phải là "Bearer <token>"
        if (bearerToken != null && !bearerToken.isEmpty()) {
            builder.addHeader("Authorization", bearerToken);
        }

        Request request = builder.build();

        // 3) Gửi async
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

                String responseBody = (response.body() != null) ? response.body().string() : "[]";

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

    /**
     * Parse JSON -> List<AIRoute> + stops (List<Place>)
     *
     * Backend thường trả:
     * [
     *   {
     *     "title": "...",
     *     "description": "...",
     *     "distanceKm": 3.5,
     *     "duration": "20m",
     *     "stops": [
     *        { "id":"...", "name":"...", "description":"...", "defaultPicture":"...", "address":"...", "latitude":..., "longitude":... }
     *     ]
     *   }
     * ]
     */
    private List<AIRoute> parseRoutes(JSONArray arr) throws JSONException {
        List<AIRoute> result = new ArrayList<>();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);

            String title = obj.optString("title", "");
            String description = obj.optString("description", "");
            double distanceKm = obj.optDouble("distanceKm", 0.0);
            String duration = obj.optString("duration", "");

            // ✅ Parse stops
            List<Place> stops = new ArrayList<>();
            JSONArray stopsArr = obj.optJSONArray("stops");
            if (stopsArr != null) {
                for (int j = 0; j < stopsArr.length(); j++) {
                    JSONObject s = stopsArr.getJSONObject(j);

                    String id = s.optString("id", "");
                    String name = s.optString("name", "");
                    String desc = s.optString("description", "");
                    String address = s.optString("address", "");

                    // picture có thể là defaultPicture hoặc pictureURL (tùy BE)
                    String picture = s.optString("defaultPicture", "");
                    if (picture.isEmpty()) {
                        picture = s.optString("pictureURL", "");
                    }

                    double lat = s.optDouble("latitude", 0.0);
                    double lng = s.optDouble("longitude", 0.0);

                    // Place constructor của bạn cần distance -> để rỗng ""
                    Place p = new Place(name, desc, "", picture, address);

                    if (!id.isEmpty()) {
                        p.setId(id); // ✅ quan trọng để click mở PlaceDetail
                    }

                    p.setLatitude(lat);
                    p.setLongitude(lng);

                    stops.add(p);
                }
            }

            AIRoute route = new AIRoute(title, description, distanceKm, duration, stops);
            result.add(route);
        }

        return result;
    }
}
