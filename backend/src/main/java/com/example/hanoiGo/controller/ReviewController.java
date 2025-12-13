package com.example.hanoiGo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hanoiGo.dto.request.ReviewRequest;

import com.example.hanoiGo.dto.response.ApiResponse;
import com.example.hanoiGo.dto.response.ReviewResponse;
import com.example.hanoiGo.service.ReviewService;
import com.example.hanoiGo.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtUtil jwtUtil;

    @GetMapping("/get-list")
    public ApiResponse<List<ReviewResponse>> getReviewsForLocation(
        @RequestParam(required = true) String address,
        @RequestParam(required = false) String sortType
    ) {
        List<ReviewResponse> response = reviewService.getListReview(address, sortType);

        return ApiResponse.<List<ReviewResponse>>builder()
                .code(1000)
                .message("Lấy danh sách đánh giá của địa điểm thành công")
                .result(response)
                .build();
    }

    @PostMapping("/add")
    public ApiResponse<String> addReview(
        @RequestBody ReviewRequest request,
        @RequestHeader("Authorization") String authHeader
    ) {
        String token = jwtUtil.extractToken(authHeader); // helper method: cắt "Bearer "
        String username = jwtUtil.getUsernameFromToken(token);
        reviewService.addReview(request, username);

        return ApiResponse.<String>builder()
                .code(1000)
                .message("Thêm đánh giá thành công")
                .result("ok")
                .build();
    }

    @PostMapping("/update")
    public ApiResponse<String> updateReview(
        @RequestBody ReviewRequest request,
        @RequestHeader("Authorization") String authHeader
    ) {
        String token = jwtUtil.extractToken(authHeader); // helper method: cắt "Bearer "
        String username = jwtUtil.getUsernameFromToken(token);
        reviewService.updateReview(request, username);

        return ApiResponse.<String>builder()
                .code(1000)
                .message("Cập nhật đánh giá thành công")
                .result("ok")
                .build();
    }

    @PostMapping("/delete")
    public ApiResponse<String> deleteReview(
        @RequestParam(required = true) String address,
        @RequestHeader("Authorization") String authHeader
    ) {
        String token = jwtUtil.extractToken(authHeader); // helper method: cắt "Bearer "
        String username = jwtUtil.getUsernameFromToken(token);
        reviewService.deleteReview(address, username);

        return ApiResponse.<String>builder()
                .code(1000)
                .message("Xóa đánh giá thành công")
                .result("ok")
                .build();
    }

    @PostMapping("/like")
    public ApiResponse<String> likeReview(
        @RequestParam(required = true) String address,
        @RequestParam(required = true) String authorName,
        @RequestHeader("Authorization") String authHeader
    ) {
        String token = jwtUtil.extractToken(authHeader); // helper method: cắt "Bearer "
        String username = jwtUtil.getUsernameFromToken(token);
        String action = reviewService.likeReview(address, authorName, username);

        return ApiResponse.<String>builder()
                .code(1000)
                .message(action + " đánh giá thành công!")
                .result("ok")
                .build();
    }

    @GetMapping("/get-liked-reviews")
    public ApiResponse<List<ReviewResponse>> getLikedReviews(
        @RequestParam(required = true) String address,
        @RequestHeader("Authorization") String authHeader
    ) {
        String token = jwtUtil.extractToken(authHeader); // helper method: cắt "Bearer "
        String username = jwtUtil.getUsernameFromToken(token);
        List<ReviewResponse> result = reviewService.getLikedReviews(address, username);

        return ApiResponse.<List<ReviewResponse>>builder()
                .code(1000)
                .message("Lấy danh sách đánh giá đã thích thành công!")
                .result(result)
                .build();
    }
}