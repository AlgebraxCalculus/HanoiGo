package com.example.myapplication.model;

import java.time.LocalDateTime;

public class Checkpoint {
    private Place place;
    private LocalDateTime date;
    private Review review;

    public Checkpoint(Place place, LocalDateTime date, Review review){
        this.place = place;
        this.date = date;
        this.review = review;
    }

    public Place getPlace() {
        return place;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Review getReview() {
        return review;
    }
}
