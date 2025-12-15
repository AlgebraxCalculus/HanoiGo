package com.example.hanoiGo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkResponse {

    private UUID id;
    private String locationId;
    private String locationName;
    private String locationAddress;
    private String locationDescription;
    private String defaultPicture;
    private double latitude;
    private double longitude;
    private String description;
    private LocalDateTime bookmarkedAt;
    private Double averageRating;
    private Long reviewCount;
}
