package com.example.hanoiGo.controller;

import com.example.hanoiGo.dto.request.BookmarkListRequest;
import com.example.hanoiGo.dto.response.ApiResponse;
import com.example.hanoiGo.dto.response.BookmarkListResponse;
import com.example.hanoiGo.service.BookmarkListService;
import com.example.hanoiGo.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookmark-lists")
@RequiredArgsConstructor
public class BookmarkListController {

    private final BookmarkListService bookmarkListService;
    private final JwtUtil jwtUtil;

    /**
     * Tạo bookmark list mới
     * POST /api/bookmark-lists/create
     */
    @PostMapping("/create")
    public ApiResponse<BookmarkListResponse> createBookmarkList(
            @Valid @RequestBody BookmarkListRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtUtil.extractToken(authHeader);
        UUID userId = jwtUtil.extractUserId(token);
        request.setUserId(userId);

        BookmarkListResponse response = bookmarkListService.createBookmarkList(request);

        return ApiResponse.<BookmarkListResponse>builder()
                .code(1000)
                .message("Tạo danh sách bookmark thành công")
                .result(response)
                .build();
    }

    /**
     * Lấy tất cả bookmark lists của user
     * GET /api/bookmark-lists/my-lists
     */
    @GetMapping("/my-lists")
    public ApiResponse<List<BookmarkListResponse>> getMyBookmarkLists(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtUtil.extractToken(authHeader);
        UUID userId = jwtUtil.extractUserId(token);

        List<BookmarkListResponse> lists = bookmarkListService.getUserBookmarkLists(userId);

        return ApiResponse.<List<BookmarkListResponse>>builder()
                .code(1000)
                .message("Lấy danh sách bookmark list thành công")
                .result(lists)
                .build();
    }

    /**
     * Xóa bookmark list
     * DELETE /api/bookmark-lists/{listId}
     */
    @DeleteMapping("/{listId}")
    public ApiResponse<Void> deleteBookmarkList(
            @PathVariable UUID listId,
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtUtil.extractToken(authHeader);
        UUID userId = jwtUtil.extractUserId(token);

        bookmarkListService.deleteBookmarkList(listId, userId);

        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Xóa danh sách bookmark thành công")
                .build();
    }
}
