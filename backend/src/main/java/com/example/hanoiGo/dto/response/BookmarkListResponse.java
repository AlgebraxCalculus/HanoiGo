package com.example.hanoiGo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkListResponse {

    private UUID id;
    private String name;
    private String icon;
    private String description;
    private long bookmarkCount;  
}
