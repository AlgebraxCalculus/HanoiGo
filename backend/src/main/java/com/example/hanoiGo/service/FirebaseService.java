package com.example.hanoiGo.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseService {
    
    private final FirebaseAuth firebaseAuth;
    
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
        
        // Lấy thông tin provider (cách đăng nhập)
        String signInProvider = decodedToken.getClaims().get("firebase").toString();
        if (signInProvider.contains("password")) {
            userInfo.setSignInProvider("email");
        } else if (signInProvider.contains("google.com")) {
            userInfo.setSignInProvider("google");
        } else if (signInProvider.contains("facebook.com")) {
            userInfo.setSignInProvider("facebook");
        } else {
            userInfo.setSignInProvider("unknown");
        }
        
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
}