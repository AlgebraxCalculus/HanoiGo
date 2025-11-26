package com.example.hanoiGo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class BookmarkListRequest {

    @NotBlank(message = "Tên danh sách không được để trống")
    private String name;

    private String icon;  // "bookmark", "heart", "flag"

    private String description;

    private UUID userId;  //  JWT
}
