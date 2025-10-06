package com.example.hanoiGo.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.example.hanoiGo.dto.request.CheckpointRequest;
import com.example.hanoiGo.dto.response.CheckpointResponse;
import com.example.hanoiGo.dto.response.LocationResponse;
import com.example.hanoiGo.dto.response.UserResponse;
import com.example.hanoiGo.exception.AppException;
import com.example.hanoiGo.exception.ErrorCode;
import com.example.hanoiGo.mapper.CheckpointMapper;
import com.example.hanoiGo.model.Checkpoint;
import com.example.hanoiGo.repository.CheckpointRepository;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckpointService {
    private final CheckpointRepository checkpointRepository;
    private final LocationService locationService;
    private final CheckpointMapper checkpointMapper;
    private final UserService userService;

    @Value("${goong.api.key}")
    private String goongApiKey;

    public CheckpointResponse checkIn(CheckpointRequest request) {

        // Gọi service đã có sẵn logic lấy place detail + distance
        LocationResponse locationResponse = locationService.getLocationDetailById(
                request.getLocationId(),
                request.getUserLatitude(),
                request.getUserLongitude()
        );

        if (locationResponse == null) {
            throw new AppException(ErrorCode.LOCATION_NOT_EXISTED);
        }

        // Kiểm tra khoảng cách tính được từ Goong DistanceMatrix
        int distanceValue = locationResponse.getDistanceValue(); // đơn vị: mét
        double allowedRadius = 100.0; // cho phép check-in trong bán kính 100m

        if (distanceValue > allowedRadius) {
            throw new RuntimeException("User is too far from location. Distance: " + distanceValue + "m");
        }

        // Lấy thông tin user
        UserResponse userResponse = userService.getUserById(request.getUserId().toString());

        // Tạo checkpoint mới
        Checkpoint checkpoint = checkpointMapper.toCheckpoint(request);

        try {
            checkpoint = checkpointRepository.save(checkpoint);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.CHECKPOINT_EXISTED);
        }

        // Trả về dữ liệu phản hồi gồm thông tin địa điểm và người dùng
        return checkpointMapper.toCheckpointResponse(checkpoint, locationResponse, userResponse);
    }
}
