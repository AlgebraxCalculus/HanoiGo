package com.example.myapplication.model;

public class Review {
    private String name;
    private String subtitle;
    private String content;
    private String time;
    private int rating;
    private int likeCount;
    private int[] imageResIds;

    public Review(String name, String subtitle, String content, String time, int rating, int likeCount, int[] imageResIds) {
        this.name = name;
        this.subtitle = subtitle;
        this.content = content;
        this.time = time;
        this.rating = rating;
        this.likeCount = likeCount;
        this.imageResIds = imageResIds;
    }

    public String getName() { return name; }
    public String getSubtitle() { return subtitle; }
    public String getContent() { return content; }
    public String getTime() { return time; }
    public int getRating() { return rating; }
    public int getLikeCount() { return likeCount; }
    public int[] getImageResIds() { return imageResIds; }
}
