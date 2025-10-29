package com.example.hanoiGo.dto.request;

import lombok.Data;

@Data
public class UpdateFcmTokenRequest {     
    private String firebaseUid;  
    private String fcmToken;
}