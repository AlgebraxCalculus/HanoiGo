package com.example.myapplication.model;

public class LeaderboardItem {
    private int rank;
    private String name;
    private int score;
    private String avatar;

    public LeaderboardItem(int rank, String name, int score, String avatar){
        this.rank = rank;
        this.name = name;
        this.score = score;
        this.avatar = avatar;
    }

    public int getRank() {
        return rank;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public String getAvatar() {
        return avatar;
    }
}


