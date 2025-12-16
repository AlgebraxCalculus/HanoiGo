package com.example.hanoiGo.dto.request;

import lombok.Data;

@Data
public class TravelPlanRequest {
    private String startLocation;
    private String travelDate;
    private int durationDays;
    private String[] interests;
    private double budget;
    private Double userLat;
    private Double userLng;
}
