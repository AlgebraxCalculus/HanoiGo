package com.example.hanoiGo.dto.response;

import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor

public class LoginResponse {
    private String token;
    private UserResponse user;
}
