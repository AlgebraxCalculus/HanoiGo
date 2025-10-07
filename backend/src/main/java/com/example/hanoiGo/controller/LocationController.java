package com.example.hanoiGo.controller;

import com.example.hanoiGo.dto.response.ApiResponse;
import com.example.hanoiGo.dto.response.LocationResponse;
import com.example.hanoiGo.service.LocationService;
import com.example.hanoiGo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/get-tags-by-id")
    public ApiResponse<List<String>> getTagsByLocationId(@RequestParam(value = "locationId") String locationId) {
        List<String> tags = locationService.getTagListByLocationID(locationId);
        return ApiResponse.<List<String>>builder()
                .code(1000)
                .message("Lấy thông tin tags của location thành công")
                .result(tags)
                .build();
    }

    @GetMapping("/get-detail-by-id")
    public ApiResponse<LocationResponse> getDetailByLocationId(
        @RequestParam(value = "locationId") String locationId
    ) {
        LocationResponse locationDetail = locationService.getLocationDetailById(locationId);
        return ApiResponse.<LocationResponse>builder()
                .code(1000)
                .message("Lấy thông tin chi tiết của location thành công")
                .result(locationDetail)
                .build();
    }


}