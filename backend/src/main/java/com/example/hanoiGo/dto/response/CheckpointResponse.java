package com.example.hanoiGo.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CheckpointResponse {
    private LocalDateTime checkedInTime;
    private String locationName;
    private String locationAddress;
    private String username;
}
