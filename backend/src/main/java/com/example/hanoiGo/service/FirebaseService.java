package com.example.hanoiGo.service;

import com.google.firebase.messaging.Notification;
import com.google.auto.value.AutoValue.Builder;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.cloud.FirestoreClient;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Builder
public class FirebaseService {
    
    private final FirebaseAuth firebaseAuth;
    private static final Logger logger = LoggerFactory.getLogger(FirebaseService.class);

    
    // Xác thực Firebase ID token
    public FirebaseToken verifyToken(String idToken) throws FirebaseAuthException {
        return firebaseAuth.verifyIdToken(idToken);
    }
    
    // Lấy thông tin user từ Firebase token
    public FirebaseUserInfo getUserInfo(String idToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = verifyToken(idToken);
        
        FirebaseUserInfo userInfo = new FirebaseUserInfo();
        userInfo.setUid(decodedToken.getUid());
        userInfo.setEmail(decodedToken.getEmail());
        userInfo.setName(decodedToken.getName());
        userInfo.setPicture(decodedToken.getPicture());
        userInfo.setEmailVerified(decodedToken.isEmailVerified());
        
        return userInfo;
    }
    
    // DTO cho thông tin Firebase user
    public static class FirebaseUserInfo {
        private String uid;
        private String email;
        private String name;
        private String picture;
        private boolean emailVerified;
        private String signInProvider; // email, google, facebook, etc.
        
        // Getters and Setters
        public String getUid() { return uid; }
        public void setUid(String uid) { this.uid = uid; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getPicture() { return picture; }
        public void setPicture(String picture) { this.picture = picture; }
        
        public boolean isEmailVerified() { return emailVerified; }
        public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
        
        public String getSignInProvider() { return signInProvider; }
        public void setSignInProvider(String signInProvider) { this.signInProvider = signInProvider; }
    }

    // Gửi thông báo FCM đến một user (qua FCM token)
    public void sendNotification(String fcmToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(
                            Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build()
                    )
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("Sent FCM notification successfully: {}", response);
        } catch (Exception e) {
            logger.error("Failed to send FCM notification", e);
        }
    }

    public void pushCheckinData(UUID userId, String locationName, int points) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("location", locationName);
            data.put("points", points);
            data.put("timestamp", LocalDateTime.now().toString());

            db.collection("checkins").add(data);
            System.out.println("Check-in data pushed to Firestore!");
        } catch (Exception e) {
            System.err.println("Failed to push check-in data to Firebase: " + e.getMessage());
        }
    }
}