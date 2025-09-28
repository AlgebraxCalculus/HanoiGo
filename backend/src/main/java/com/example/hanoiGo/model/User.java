package com.example.hanoiGo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer points = 0;
    
    @Column(name = "rank")
    private Integer rank;
    
    // Thêm các trường cho Firebase authentication
    @Column(name = "firebase_uid", unique = true)
    private String firebaseUid;
    
    // @Column(name = "full_name")
    // private String fullName;
    
    @Column(name = "profile_picture")
    private String profilePicture;
    
    // @Column(name = "sign_in_provider")
    // private String signInProvider; // google, email, etc.
}
