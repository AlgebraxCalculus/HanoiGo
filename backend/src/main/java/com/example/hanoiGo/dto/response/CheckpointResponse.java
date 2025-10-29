package com.example.hanoiGo.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CheckpointResponse {
    private LocationResponse location;
    private LocalDateTime checkedInTime;
    private ReviewResponse review;
}
