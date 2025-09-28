package com.example.hanoiGo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private UUID id;
    private String username;
    private String email;
    // private String fullName;
    private String profilePicture;
    // private String signInProvider;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private Integer points;
    private Integer rank;
    
    // DTO cho Firebase login request
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FirebaseLoginRequest {
        private String firebaseToken;
    }
    
    // DTO cho phản hồi đăng nhập
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String token;
        private UserDTO user;
    }
}
