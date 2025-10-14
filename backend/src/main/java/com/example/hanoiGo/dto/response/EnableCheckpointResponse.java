package com.example.hanoiGo.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnableCheckpointResponse {
    private String locationId;
    private String locationAddress;
    private int distanceValue; 
}