package com.example.myapplication.api;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import org.json.JSONObject;
import java.io.IOException;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FirebaseMessagingApi extends FirebaseMessagingService {
    private static final String BASE_URL = "http://192.168.1.174:8080/api/users";
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        System.out.println("fcmToken: "+token);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String jwtToken = prefs.getString("jwt_token", null);

        // TODO: Gửi token lên backend
        sendTokenToServer(jwtToken, token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("FCM", "🔥 FCM message received!");

        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            Log.d("FCM", "🔔 showNotification called with title=" + title + ", body=" + body);
            showNotification(title, body);
        }
    }

    private void showNotification(String title, String messageBody) {
        String channelId = "default_channel_id";
        String channelName = "General";

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }


    public void sendTokenToServer(String jwtToken, String token) {
        // Ví dụ dùng OkHttp hoặc Retrofit để gọi API backend
        JSONObject json = new JSONObject();
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                json.put("firebaseUid", FirebaseAuth.getInstance().getCurrentUser().getUid());
            } else {
                json.put("firebaseUid", JSONObject.NULL);
            }

            json.put("fcmToken", token);

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );
            Request request = new Request.Builder()
                    .url(BASE_URL+"/update-fcm-token")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + jwtToken)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("fcmToken failed: "+token);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        System.out.println("fcmToken success: "+token);
                    } else {
                        Log.e("FCM", "⚠️ Backend error: " + response.code());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
