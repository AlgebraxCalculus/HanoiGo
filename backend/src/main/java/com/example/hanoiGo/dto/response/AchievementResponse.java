package com.example.hanoiGo.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AchievementResponse {
    private String name;
    private String description;
    private String tier;
    private LocalDateTime earned_at;
}
