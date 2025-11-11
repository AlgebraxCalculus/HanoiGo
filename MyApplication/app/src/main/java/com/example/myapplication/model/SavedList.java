package com.example.myapplication.model;

import com.example.myapplication.R;

public class SavedList {
    private String id;  // UUID from backend
    private String iconType;  // "bookmark", "heart", "flag"
    private String title;
    private long placeCount;

    public SavedList(String id, String iconType, String title, long placeCount) {
        this.id = id;
        this.iconType = iconType;
        this.title = title;
        this.placeCount = placeCount;
    }

    // Constructor cũ để tương thích
    public SavedList(int iconResId, String title, int placeCount) {
        this.id = null;
        this.iconType = iconResIdToType(iconResId);
        this.title = title;
        this.placeCount = placeCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIconType() {
        return iconType;
    }

    public void setIconType(String iconType) {
        this.iconType = iconType;
    }

    public int getIconResId() {
        return iconTypeToResId(iconType);
    }

    public void setIconResId(int iconResId) {
        this.iconType = iconResIdToType(iconResId);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getPlaceCount() {
        return placeCount;
    }

    public void setPlaceCount(long placeCount) {
        this.placeCount = placeCount;
    }

    // Helper methods để convert giữa iconType (string) và iconResId (int)
    private static String iconResIdToType(int resId) {
        if (resId == R.drawable.ic_heart) return "heart";
        if (resId == R.drawable.ic_flag) return "flag";
        return "bookmark";
    }

    private static int iconTypeToResId(String type) {
        if ("heart".equals(type)) return R.drawable.ic_heart;
        if ("flag".equals(type)) return R.drawable.ic_flag;
        return R.drawable.ic_bookmark;
    }
}
