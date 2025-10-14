package com.example.hanoiGo.controller;

import com.example.hanoiGo.dto.response.ApiResponse;
import com.example.hanoiGo.dto.response.LocationResponse;
import com.example.hanoiGo.dto.response.LocationListResponse;
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

    //API lấy danh sách location gồm param: locationId, lat, lng, tag, mostVisited, nearest, limit
    @GetMapping("/get-list")
    public ApiResponse<List<LocationListResponse>> getListLocation(
        @RequestParam(value = "lat") Double lat,
        @RequestParam(value = "lng") Double lng,
        @RequestParam(value = "tag", required = false) String tag,
        @RequestParam(value = "mostVisited", required = false) Boolean mostVisited,
        @RequestParam(value = "nearest", required = false) Boolean nearest,
        @RequestParam(value = "limit", required = false) Integer limit
    ) {
        List<LocationListResponse> locationList = locationService.getListLocation(
            lat, lng, tag, mostVisited, nearest, limit
        );

        String message = "Lấy danh sách location thành công";
        if(tag != null && !tag.isEmpty()) message = "Lấy danh sách location có tag '" + tag + "' thành công";
        else if(mostVisited != null && mostVisited) message = "Lấy danh sách location theo số lượt checked-in giảm dần thành công";
        else if(nearest != null && nearest) message = "Lấy danh sách location theo thứ tự khoảng cách tăng dần thành công";
        return ApiResponse.<List<LocationListResponse>>builder()
                .code(1000)
                .message(message)
                .result(locationList)
                .build();
    }

    // Lấy locationId theo address
    @GetMapping("/get-id-by-address")
    public ApiResponse<String> getLocationIdByAddress(
        @RequestParam(value = "address") String address) {
        String locationId = locationService.getLocationIdByAddress(address);
        return ApiResponse.<String>builder()
                .code(1000)
                .message("Lấy locationId theo address thành công")
                .result(locationId)
                .build();
    }
}