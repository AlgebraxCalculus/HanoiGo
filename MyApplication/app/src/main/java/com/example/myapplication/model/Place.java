package com.example.myapplication.model;

import java.io.Serializable;
import java.util.List;

public class Place implements Serializable {
    private String name;
    private String description;
    private String distance;
    private String pictureURL;
    private List<String> imageUrls;

    private double latitude;  // Vĩ độ
    private double longitude; // Kinh độ

    public Place(String name, String description, String distance, String pictureURL) {
    // 🌟 Cập nhật Constructor
    public Place(String name, String description, String distance, List<String> imageUrls, double latitude, double longitude) {
        this.name = name;
        this.description = description;
        this.distance = distance;
        this.pictureURL = pictureURL;
        this.imageUrls = imageUrls;
        this.latitude = latitude;
        this.longitude = longitude;
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
    public List<String> getImageUrls() {
        return imageUrls;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}