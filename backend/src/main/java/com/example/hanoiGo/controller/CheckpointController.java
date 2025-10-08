// CheckpointController.java - Fix: Thêm endpoint /enable-checkin, cập nhật /checkin cho single response

package com.example.hanoiGo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hanoiGo.dto.request.CheckpointRequest;
import com.example.hanoiGo.dto.response.ApiResponse;
import com.example.hanoiGo.dto.response.CheckpointResponse;
import com.example.hanoiGo.dto.response.EnableCheckpointResponse; 
import com.example.hanoiGo.service.CheckpointService;
import com.example.hanoiGo.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/checkpoints")
@RequiredArgsConstructor
public class CheckpointController {

    private final CheckpointService checkpointService;
    private final JwtUtil jwtUtil;

    @PostMapping("/enable-checkin")
    public ApiResponse<List<EnableCheckpointResponse>> enableCheckIn(
            @Valid @RequestBody CheckpointRequest request,
            @RequestHeader("Authorization") String authHeader) {

        // Lấy token từ header "Bearer <token>"
        String token = jwtUtil.extractToken(authHeader);

        // Lấy userId từ token
        UUID userId = jwtUtil.extractUserId(token);

        // Gán userId vào request 
        request.setUserId(userId);

        // Gọi service enable check-in
        List<EnableCheckpointResponse> response = checkpointService.enableCheckIn(request);

        // Trả về kết quả API
        return ApiResponse.<List<EnableCheckpointResponse>>builder()
                .code(1000)
                .message("Lấy danh sách địa điểm có thể check-in thành công")
                .result(response)
                .build();
    }

    @PostMapping("/checkin")
    public ApiResponse<CheckpointResponse> checkIn(  
            @Valid @RequestBody CheckpointRequest request,
            @RequestHeader("Authorization") String authHeader) {

        // Lấy token từ header "Bearer <token>"
        String token = jwtUtil.extractToken(authHeader);

        // Lấy userId từ token
        UUID userId = jwtUtil.extractUserId(token);

        // Gán userId vào request 
        request.setUserId(userId);

        CheckpointResponse response = checkpointService.checkIn(request);  

        // Trả về kết quả API
        return ApiResponse.<CheckpointResponse>builder()
                .code(1000)
                .message("Check-in địa điểm thành công")
                .result(response)
                .build();
    }
}