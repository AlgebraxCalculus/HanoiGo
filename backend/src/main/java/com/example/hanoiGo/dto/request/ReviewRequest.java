package com.example.hanoiGo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    private String locationAddress;
    private Integer rating;
    private String content;
    private List<String> pictureUrls;
}
