package com.example.myapplication.model;

public class Achievement {
    private String title;

    private String description;

    private String badgeLevel;

    private int resBadgeImage;

    public Achievement(String title, String description, String badgeLevel, int resBadgeImage){
        this.title = title;
        this.description = description;
        this.badgeLevel = badgeLevel;
        this.resBadgeImage = resBadgeImage;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getBadgeLevel() {
        return badgeLevel;
    }

    public int getResBadgeImage() {
        return resBadgeImage;
    }
}
