package com.example.hanoiGo.controller;

import com.example.hanoiGo.dto.request.BookmarkRequest;
import com.example.hanoiGo.dto.response.ApiResponse;
import com.example.hanoiGo.dto.response.BookmarkResponse;
import com.example.hanoiGo.service.BookmarkService;
import com.example.hanoiGo.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final JwtUtil jwtUtil;

    /**
     * Thêm bookmark vào 1 bookmark list
     * POST /api/bookmarks/add
     */
    @PostMapping("/add")
    public ApiResponse<BookmarkResponse> addBookmark(
            @Valid @RequestBody BookmarkRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtUtil.extractToken(authHeader);
        UUID userId = jwtUtil.extractUserId(token);
        request.setUserId(userId);

        BookmarkResponse response = bookmarkService.addBookmark(request);

        return ApiResponse.<BookmarkResponse>builder()
                .code(1000)
                .message("Đã thêm bookmark thành công")
                .result(response)
                .build();
    }

    /**
     * Xóa bookmark khỏi bookmark list
     * DELETE /api/bookmarks/remove
     */
    @DeleteMapping("/remove")
    public ApiResponse<Void> removeBookmark(
            @Valid @RequestBody BookmarkRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtUtil.extractToken(authHeader);
        UUID userId = jwtUtil.extractUserId(token);
        request.setUserId(userId);

        bookmarkService.removeBookmark(request);

        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Đã xóa bookmark thành công")
                .build();
    }

    /**
     * Lấy tất cả bookmarks trong 1 bookmark list
     * GET /api/bookmarks/list/{listId}
     */
    @GetMapping("/list/{listId}")
    public ApiResponse<List<BookmarkResponse>> getBookmarkListBookmarks(
            @PathVariable UUID listId,
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtUtil.extractToken(authHeader);
        UUID userId = jwtUtil.extractUserId(token);

        List<BookmarkResponse> bookmarks = bookmarkService.getBookmarkListBookmarks(listId, userId);

        return ApiResponse.<List<BookmarkResponse>>builder()
                .code(1000)
                .message("Lấy danh sách bookmark thành công")
                .result(bookmarks)
                .build();
    }

    /**
     * Lấy TẤT CẢ bookmarks của user (qua tất cả bookmark lists)
     * GET /api/bookmarks/all
     */
    @GetMapping("/all")
    public ApiResponse<List<BookmarkResponse>> getAllUserBookmarks(
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtUtil.extractToken(authHeader);
        UUID userId = jwtUtil.extractUserId(token);

        List<BookmarkResponse> bookmarks = bookmarkService.getAllUserBookmarks(userId);

        return ApiResponse.<List<BookmarkResponse>>builder()
                .code(1000)
                .message("Lấy tất cả bookmark thành công")
                .result(bookmarks)
                .build();
    }

    /**
     * Kiểm tra location đã được bookmark trong list chưa
     * GET /api/bookmarks/check?locationId=...&bookmarkListId=...
     */
    @GetMapping("/check")
    public ApiResponse<Boolean> checkBookmark(
            @RequestParam String locationId,
            @RequestParam UUID bookmarkListId,
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtUtil.extractToken(authHeader);
        UUID userId = jwtUtil.extractUserId(token);

        boolean isBookmarked = bookmarkService.isBookmarked(bookmarkListId, locationId);

        return ApiResponse.<Boolean>builder()
                .code(1000)
                .message("Kiểm tra bookmark thành công")
                .result(isBookmarked)
                .build();
    }

    /**
     * Đếm số bookmarks trong 1 bookmark list
     * GET /api/bookmarks/count/{listId}
     */
    @GetMapping("/count/{listId}")
    public ApiResponse<Long> countBookmarks(
            @PathVariable UUID listId,
            @RequestHeader("Authorization") String authHeader) {

        String token = jwtUtil.extractToken(authHeader);
        UUID userId = jwtUtil.extractUserId(token);

        long count = bookmarkService.countBookmarkListBookmarks(listId);

        return ApiResponse.<Long>builder()
                .code(1000)
                .message("Đếm bookmark thành công")
                .result(count)
                .build();
    }
}
