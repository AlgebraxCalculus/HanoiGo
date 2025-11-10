package com.example.myapplication.model;

import java.time.LocalDate;

public class Achievement {
    private String title;

    private String description;

    private String badgeLevel;

    private int resBadgeImage;

    private LocalDate date;

    public Achievement(String title, String description, String badgeLevel, int resBadgeImage){
        this.title = title;
        this.description = description;
        this.badgeLevel = badgeLevel;
        this.resBadgeImage = resBadgeImage;
    }

    public Achievement(String title, String description, String badgeLevel, int resBadgeImage, LocalDate date){
        this.title = title;
        this.description = description;
        this.badgeLevel = badgeLevel;
        this.resBadgeImage = resBadgeImage;
        this.date = date;
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

    public LocalDate getDate() {return date;}
}
