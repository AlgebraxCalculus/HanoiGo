package com.example.hanoiGo.dto.response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {
    private String name;
    private String address;
    private String description;
    private List<String> tags;
    private double latitude;
    private double longitude;
    private String defaultPicture;
    private String distance;
    private int distanceValue;
}
