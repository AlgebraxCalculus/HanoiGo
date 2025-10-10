package com.example.myapplication.model;

public class LeaderboardItem {
    private int rank;
    private String name;
    private int score;
    private int avatarRes;

    public LeaderboardItem(int rank, String name, int score, int avatarRes){
        this.rank = rank;
        this.name = name;
        this.score = score;
        this.avatarRes = avatarRes;
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

    public int getAvatarRes() {
        return avatarRes;
    }
}


