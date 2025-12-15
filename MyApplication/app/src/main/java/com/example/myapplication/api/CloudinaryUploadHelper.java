package com.example.myapplication.api;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class CloudinaryUploadHelper {

    // Thay đổi các giá trị này theo Cloudinary của bạn
    private static final String CLOUD_NAME = "du9ycfajs";
    private static final String UPLOAD_PRESET = "ml_default"; // Tạo unsigned upload preset trong Cloudinary
    private static final String UPLOAD_URL = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

    public interface UploadCallback {
        void onSuccess(List<String> imageUrls);
        void onFailure(String errorMessage);
        void onProgress(int current, int total);
    }

    /**
     * Upload nhiều ảnh lên Cloudinary
     * @param context Context
     * @param imageUris List URI của ảnh từ thiết bị
     * @param callback Callback trả về kết quả
     */
    public static void uploadImages(Context context, List<Uri> imageUris, UploadCallback callback) {
        List<String> uploadedUrls = new ArrayList<>();
        final int totalImages = imageUris.size();
        final int[] uploadedCount = {0};
        final boolean[] hasError = {false};

        if (imageUris.isEmpty()) {
            callback.onFailure("No images to upload");
            return;
        }

        for (int i = 0; i < imageUris.size(); i++) {
            Uri imageUri = imageUris.get(i);
            final int currentIndex = i;

            uploadSingleImage(context, imageUri, new SingleUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    synchronized (uploadedUrls) {
                        uploadedUrls.add(imageUrl);
                        uploadedCount[0]++;
                        callback.onProgress(uploadedCount[0], totalImages);

                        // Nếu đã upload hết tất cả ảnh
                        if (uploadedCount[0] == totalImages) {
                            if (!hasError[0]) {
                                callback.onSuccess(uploadedUrls);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    synchronized (uploadedUrls) {
                        hasError[0] = true;
                        callback.onFailure("Failed to upload image " + (currentIndex + 1) + ": " + errorMessage);
                    }
                }
            });
        }
    }

    private interface SingleUploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }

    /**
     * Upload 1 ảnh lên Cloudinary
     */
    private static void uploadSingleImage(Context context, Uri imageUri, SingleUploadCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            // Đọc ảnh từ URI và convert sang byte array
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] imageBytes = byteBuffer.toByteArray();
            inputStream.close();

            // Convert sang Base64
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            String dataUrl = "data:image/jpeg;base64," + base64Image;

            // Tạo request body
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", dataUrl)
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .build();

            Request request = new Request.Builder()
                    .url(UPLOAD_URL)
                    .post(requestBody)
                    .build();

            // Gửi request
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseBody);
                            String secureUrl = jsonObject.getString("secure_url");
                            callback.onSuccess(secureUrl);
                        } catch (JSONException e) {
                            callback.onFailure("Failed to parse response: " + e.getMessage());
                        }
                    } else {
                        callback.onFailure("Upload failed with code: " + response.code());
                    }
                }
            });

        } catch (IOException e) {
            callback.onFailure("Failed to read image: " + e.getMessage());
        }
    }
}