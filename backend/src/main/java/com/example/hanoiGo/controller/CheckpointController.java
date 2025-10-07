package com.example.hanoiGo.controller;

import java.util.List;
// import org.hibernate.validator.constraints.UUID; // Remove this line
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hanoiGo.dto.request.CheckpointRequest;
import com.example.hanoiGo.dto.response.ApiResponse;
import com.example.hanoiGo.dto.response.CheckpointResponse;
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

    @PostMapping("/checkin")
    public ApiResponse<List<CheckpointResponse>> checkIn(
            @Valid @RequestBody CheckpointRequest request,
            @RequestHeader("Authorization") String authHeader) {

        // Lấy token từ header "Bearer <token>"
        String token = jwtUtil.extractToken(authHeader);

        // Lấy userId từ token
        UUID userId = jwtUtil.extractUserId(token);

        // Gán userId vào request
        request.setUserId(userId);

        // Gọi service check-in
        List<CheckpointResponse> response = checkpointService.checkIn(request);

        // Trả về kết quả API
        return ApiResponse.<List<CheckpointResponse>>builder()
                .code(1000)
                .message("Check-in địa điểm thành công")
                .result(response)
                .build();
    }
}