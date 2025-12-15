package com.example.hanoiGo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuggestedRouteResponse {
    private String title;
    private String description;
    private double distanceKm;
    private String duration;
    private List<LocationResponse> stops;
}
