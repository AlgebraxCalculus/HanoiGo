package com.example.hanoiGo.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String username;
    private String email;
    private String profilePicture;
    private Integer points;
    private Integer rank;
}
