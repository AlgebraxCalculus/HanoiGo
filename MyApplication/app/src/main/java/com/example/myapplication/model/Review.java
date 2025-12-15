package com.example.myapplication.model;

import java.time.LocalDateTime;
import java.util.List;

public class Review {
    private String name;
    private String content;
    private String time;
    private int rating;
    private int likeCount;
    private String avatar;
    private List<String> imageUrls;
    private boolean isLiked;

    public Review(String name, String content, String time, int rating, int likeCount, String avatar, List<String> imageUrls) {
        this.name = name;
        this.content = content;
        this.time = time;
        this.rating = rating;
        this.likeCount = likeCount;
        this.avatar = avatar;
        this.imageUrls = imageUrls;
        this.isLiked = false;
    }

    public Review(String name, String content, String time, int rating){
        this.name = name;
        this.content = content;
        this.time = time;
        this.rating = rating;
    }

    public String getName() { return name; }
    public String getContent() { return content; }
    public String getTime() { return time; }
    public int getRating() { return rating; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; } // 🌟 Thêm setter này
    public List<String> getImageUrls() { return imageUrls; }
    public boolean getIsLiked() { return isLiked; }
    public void setIsLiked(boolean isLiked) { this.isLiked = isLiked; }
    public String getAvatar() { return avatar; }
}
