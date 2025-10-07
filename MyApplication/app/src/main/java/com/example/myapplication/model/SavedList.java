package com.example.myapplication.model;

public class SavedList {
    private int iconResId;
    private String title;
    private int placeCount;

    public SavedList(int iconResId, String title, int placeCount) {
        this.iconResId = iconResId;
        this.title = title;
        this.placeCount = placeCount;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPlaceCount() {
        return placeCount;
    }

    public void setPlaceCount(int placeCount) {
        this.placeCount = placeCount;
    }
}
