package com.example.hanoiGo.dto.request;

import java.util.UUID;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CheckpointRequest {
    private UUID userId;
    private String locationAddress;
    private double userLatitude;
    private double userLongitude;
}
