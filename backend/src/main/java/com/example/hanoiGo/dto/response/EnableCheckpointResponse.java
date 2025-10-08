package com.example.hanoiGo.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnableCheckpointResponse {
    private String locationId;
    private String locationName;
    private int distanceValue; 
}