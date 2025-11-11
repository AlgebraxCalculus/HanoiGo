package com.example.hanoiGo.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnableCheckpointResponse {
    private LocationResponse locationResponse;
    private int distanceValue; 
}