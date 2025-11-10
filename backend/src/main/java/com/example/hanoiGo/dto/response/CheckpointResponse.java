package com.example.hanoiGo.dto.response;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;     
import lombok.AllArgsConstructor;    
import lombok.Builder; 

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckpointResponse {
    private LocationResponse location;
    private LocalDateTime checkedInTime;
    private ReviewResponse review;
}
