package com.example.hanoiGo.service;

import com.google.firebase.messaging.Notification;
import com.google.auto.value.AutoValue.Builder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.cloud.FirestoreClient;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import lombok.RequiredArgsConstructor;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.hanoiGo.exception.AppException;
import com.example.hanoiGo.exception.ErrorCode;
import com.example.hanoiGo.model.User;
import com.example.hanoiGo.model.UserAchievement;
import com.example.hanoiGo.repository.AchievementRepository;
import com.example.hanoiGo.repository.UserAchievementRepository;
import com.example.hanoiGo.repository.UserRepository;
import com.example.hanoiGo.service.UserService;


@Service
@RequiredArgsConstructor
@Builder
public class FirebaseService {
    
    private final FirebaseAuth firebaseAuth;
    private static final Logger logger = LoggerFactory.getLogger(FirebaseService.class);
    private final Firestore db = FirestoreClient.getFirestore();
    private final UserAchievementRepository userAchievementRepository;
    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;

    @Lazy
    @Autowired
    private UserService userService;

    
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

    //==============================================

    @PostConstruct
    public void startFirestoreListener() {
        final Map<String, Map<String, Object>> lastSnapshot = new HashMap<>();
        //Lắng nghe thay đổi trong collection "userStats"
        db.collection("userStats").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                System.err.println("Listen failed: " + e);
                return;
            }
            System.out.println("🔥 Firestore listener started!");
            if (snapshots == null) return;

            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                String docId = dc.getDocument().getId();
                Map<String, Object> after = dc.getDocument().getData();
                Map<String, Object> before = lastSnapshot.getOrDefault(docId, Collections.emptyMap());
                // Cập nhật snapshot cũ
                lastSnapshot.put(docId, after);

                if (dc.getType() == DocumentChange.Type.MODIFIED) {
                    System.out.println("New snapshot data: " + after);
                    System.out.println("Previous snapshot data: " + before);
                    handleUserStatsUpdate(before, after);
                }
            }   
        });
        System.out.println("🔥 Firestore listener for userStats started.");
    }

    private void handleUserStatsUpdate(Map<String, Object> before, Map<String, Object> after) {
        try {
            // Tìm các field vừa thay đổi
            List<String> changedFields = after.entrySet().stream()
                            .filter(e2 -> !Objects.equals(e2.getValue(), before.get(e2.getKey())))
                            .map(Map.Entry::getKey)
                            .toList();

            System.out.println("🔍 Changed fields: " + changedFields);
            String userId = String.valueOf(after.get("userId"));
            User userEntity = userRepository.findUserById(UUID.fromString(userId)).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            List<String> unlockedIds = new ArrayList<>();

            // Duyệt qua từng field thay đổi
            for (String field : changedFields) {
                int currentValue = ((Number) after.get(field)).intValue();

                // 3️⃣ Lấy danh sách achievement có field trùng
                ApiFuture<QuerySnapshot> future = db.collection("achievements")
                        .whereEqualTo("field", field)
                        .get();
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();

                for (QueryDocumentSnapshot doc : docs) {
                    Map<String, Object> ach = doc.getData();
                    int condition = ((Number) ach.get("get_condition")).intValue();

                    if (!field.equals("rank") && currentValue >= condition || field.equals("rank") && currentValue <= condition && currentValue > 0) {
                        unlockedIds.add(ach.get("achievementId").toString());
                    }
                }
            }
            System.out.println("🔓 Unlocked achievement IDs: " + unlockedIds);
            
            int achievementUnlockedCount = 0;
            List<UserAchievement> achievements = userAchievementRepository.findByUserId(UUID.fromString(userId));
            for(int i=0;i<unlockedIds.size();i++) {
                int ok = 0;
                for(UserAchievement ua : achievements) {
                    if(ua.getAchievement().getId().toString().equals(unlockedIds.get(i))) {
                        ok = 1;
                        break;
                    }
                }
                if(ok == 0){
                    UserAchievement ua = new UserAchievement();
                    ua.setUser(userEntity);
                    ua.setAchievement(achievementRepository.findById(UUID.fromString(unlockedIds.get(i))).orElse(null));
                    ua.setEarnedAt(LocalDateTime.now());
                    achievementUnlockedCount++;

                    // Gửi thông báo FCM
                    if (userEntity != null && userEntity.getFcmToken() != null) {
                        String title = "🎉 Achievement Unlocked!";
                        String body = "You have unlocked a new achievement:\n" + ua.getAchievement().getName() + " - " + ua.getAchievement().getDescription();
                        sendNotification(userEntity.getFcmToken(), title, body);
                    }

                    // Thêm vào bảng user_achievements
                    userAchievementRepository.save(ua);
                    System.out.println("✅ New achievement unlocked: " + unlockedIds.get(i));
                }
            }
            System.out.println("Total new achievements unlocked: " + achievementUnlockedCount);

            // Cập nhật lại achievement_count trong userStats
            if(achievementUnlockedCount > 0) pushUserStatsData(UUID.fromString(userId), "achievement_count", achievementUnlockedCount + ((Number) after.get("achievement_count")).intValue());
        } catch (Exception ex) {
            System.err.println("⚠️ Error processing Firestore update: " + ex.getMessage());
        }
    }

    // Gửi thông báo FCM đến một user (qua FCM token)
    public void sendNotification(String fcmToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .putData("title", title)
                    .putData("body", body)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Sent FCM notification successfully: " + response);
        } catch (Exception e) {
            System.err.println("Failed to send FCM notification: " + e.getMessage());
        }
    }

    public void pushUserStatsData(UUID userId, String field, int newValue) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put(field, newValue);

            //nhớ phải cập nhật points trong db postgres trước khi gọi hàm này thì rank mới được cập nhật đúng
            //cập nhật cả rank nếu points cũng cập nhật
            if(field.equals("points")){
                int rank = userService.getMyRank(userRepository.findUserById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)).getUsername());
                data.put("rank", rank);
            }

            ApiFuture<WriteResult> future = db.collection("userStats")
                    .document(userId.toString())
                    .set(data, SetOptions.merge());

            // Chờ ghi xong và in thời gian cập nhật
            WriteResult result = future.get();
            System.out.println("User stats updated successfully at: " + result.getUpdateTime());
        } catch (Exception e) {
            System.err.println("Exception while updating Firestore: " + e.getMessage());
        }
    }
}