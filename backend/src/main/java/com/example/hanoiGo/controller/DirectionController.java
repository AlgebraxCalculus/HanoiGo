package com.example.hanoiGo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.hanoiGo.dto.response.ApiResponse;
import com.example.hanoiGo.dto.response.DirectionResponse;
import com.example.hanoiGo.service.DirectionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/directions")
@RequiredArgsConstructor
public class DirectionController {

    private final DirectionService directionService;

    @GetMapping
    public ApiResponse<DirectionResponse> getDirection(
            @RequestParam double originLat,
            @RequestParam double originLng,
            @RequestParam double destLat,
            @RequestParam double destLng
    ) {
        DirectionResponse result =
                directionService.getDirection(originLat, originLng, destLat, destLng);

        return ApiResponse.<DirectionResponse>builder()
                .code(1000)
                .message("Get direction successfully")
                .result(result)
                .build();
    }
}