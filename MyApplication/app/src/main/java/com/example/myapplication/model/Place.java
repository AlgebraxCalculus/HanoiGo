package com.example.myapplication.model;

public class Place {
    private String name;
    private String description;
    private String distance;
    private int imageResId;

    public Place(String name, String description, String distance, int imageResId) {
        this.name = name;
        this.description = description;
        this.distance = distance;
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

    public int getImageResId() {
        return imageResId;
    }
}
