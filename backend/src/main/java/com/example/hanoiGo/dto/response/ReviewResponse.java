package com.example.hanoiGo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private UserResponse userResponse;
    private int rating;
    private LocalDateTime createdAt;
    private String content;
    private List<String> pictureUrl;
    private long likeCount;
}
