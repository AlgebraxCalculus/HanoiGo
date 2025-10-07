package com.example.hanoiGo.dto.response;

import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor

public class LocationListResponse {
    private String distanceText;
    private int distanceValue;
    private LocationResponse locationResponse;
}