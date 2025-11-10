package com.example.hanoiGo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BookmarkRequest {

    @NotBlank(message = "Location ID không được để trống")
    private String locationId;

    @NotNull(message = "Bookmark List ID không được để trống")
    private UUID bookmarkListId;

    // userId sẽ được set từ JWT token để validate quyền sở hữu
    private UUID userId;
}
