package com.example.myapplication.model;

import java.util.List;

public class TravelPlan {
    private String travelDate;
    private int durationDays;
    private List<String> interests;
    private Long budget;

    private Double userLat;
    private Double userLng;

    public TravelPlan(String travelDate, int durationDays,
                             List<String> interests, Long budget) {
        this.travelDate = travelDate;
        this.durationDays = durationDays;
        this.interests = interests;
        this.budget = budget;
        this.userLat = userLat;
        this.userLng = userLng;
    }

    public String getTravelDate() {
        return travelDate;
    }
    public int getDurationDays() {
        return durationDays;
    }
    public List<String> getInterests() {
        return interests;
    }
    public Long getBudget() {
        return budget;
    }

    public Double getUserLat() {
        return userLat;
    }

    public Double getUserLng() {
        return userLng;

    }
}
