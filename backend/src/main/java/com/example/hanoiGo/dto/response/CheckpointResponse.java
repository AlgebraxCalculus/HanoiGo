package com.example.hanoiGo.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CheckpointResponse {
    private String locationAddress;
    private LocalDateTime checkedInTime;
    private String userName;
    private int userPoint;
}
