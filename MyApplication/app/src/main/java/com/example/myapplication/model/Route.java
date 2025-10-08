package com.example.myapplication.model;

public class Route {
    private String name;
    private String description;
    private String distance;
    private String duration;
    private int imageResId;

    public Route(String name, String description, String distance, String duration, int imageResId) {
        this.name = name;
        this.description = description;
        this.distance = distance;
        this.duration = duration;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getDetails() {
        return distance + " - " + duration;
    }
}
