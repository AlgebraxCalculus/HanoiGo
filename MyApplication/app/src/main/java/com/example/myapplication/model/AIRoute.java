package com.example.myapplication.model;

import java.util.List;

public class AIRoute {
    private String title;
    private String description;
    private double distanceKm;
    private String duration;
    private List<Place> stops;

    public AIRoute() {

    }

    public AIRoute(String title,
                   String description,
                   double distanceKm,
                   String duration,
                   List<Place> stops) {
        this.title = title;
        this.description = description;
        this.distanceKm = distanceKm;
        this.duration = duration;
        this.stops = stops;
    }

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public double getDistanceKm() {
        return distanceKm;
    }
    public String getDuration() {
        return duration;
    }
    public List<Place> getStops() {
        return stops;
    }
    public void setStops(List<Place> stops) { this.stops = stops; }
}
