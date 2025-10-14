package com.example.myapplication.model;

public class Place {
    private String name;
    private String description;
    private String distance;
    private String pictureURL;

    public Place(String name, String description, String distance, String pictureURL) {
        this.name = name;
        this.description = description;
        this.distance = distance;
        this.pictureURL = pictureURL;
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

    public String getPictureURL() {
        return pictureURL;
    }
}
