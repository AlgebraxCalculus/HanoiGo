package com.example.hanoiGo.dto.response;

import lombok.Data;

@Data
public class ChartDataDailyResponse {
    private String date;
    private int points;
    private int checkpoints;
    private int rank;
}