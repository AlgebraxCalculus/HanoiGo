package com.example.myapplication.model;

import java.io.Serializable;

public class Place implements Serializable {
    private String id;
    private String name;
    private String description;
    private String distance;
    private String pictureURL;
    private double latitude;
    private double longitude;

    private String address;

    public Place(String name, String description, String distance, String pictureURL) {
        this.name = name;
        this.description = description;
        this.distance = distance;
        this.pictureURL = pictureURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}