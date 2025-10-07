package com.example.hanoiGo.controller;

import com.example.hanoiGo.dto.response.AchievementResponse;
import com.example.hanoiGo.dto.response.ApiResponse;
import com.example.hanoiGo.service.AchievementService;
import com.example.hanoiGo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;
    private final JwtUtil jwtUtil;

    @GetMapping("/me")
    public ApiResponse<List<AchievementResponse>> getAchievementsForCurrentUser(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(value = "tier", required = false) String tierSort,
            @RequestParam(value = "earned_at", required = false) String earnedAtSort) {
        String jwtToken = jwtUtil.extractToken(authorizationHeader); // Lấy JWT token từ header
        System.out.println("JWT Token: " + jwtToken);
        List<AchievementResponse> achievements = achievementService.getAchievementsForCurrentUser(jwtToken, tierSort, earnedAtSort);
        return ApiResponse.<List<AchievementResponse>>builder()
                .code(1000)
                .message("Lấy thông tin achievements của user thành công")
                .result(achievements)
                .build();
    }

    // lấy tổng số achievement đã đạt được
    @GetMapping("my-total")
    public ApiResponse<Integer> getTotalAchievementsForCurrentUser(
            @RequestHeader("Authorization") String authorizationHeader) {
        String jwtToken = jwtUtil.extractToken(authorizationHeader); // Lấy JWT token từ header
        String username = jwtUtil.getUsernameFromToken(jwtToken);
        int total = achievementService.getTotalAchievementsForCurrentUser(username);
        return ApiResponse.<Integer>builder()
                .code(1000)
                .message("Lấy tổng số achievements của user thành công")
                .result(total)
                .build();
    }
}