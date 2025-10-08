package com.example.hanoiGo.service;

import org.springframework.stereotype.Service;

import com.example.hanoiGo.dto.request.CheckpointRequest;
import com.example.hanoiGo.dto.response.CheckpointResponse;
import com.example.hanoiGo.dto.response.EnableCheckpointResponse;
import com.example.hanoiGo.dto.response.LocationListResponse;
import com.example.hanoiGo.dto.response.LocationResponse;
import com.example.hanoiGo.dto.response.UserResponse;
import com.example.hanoiGo.exception.AppException;
import com.example.hanoiGo.exception.ErrorCode;
import com.example.hanoiGo.mapper.CheckpointMapper;
import com.example.hanoiGo.model.Checkpoint;
import com.example.hanoiGo.model.LocationDetail;
import com.example.hanoiGo.model.User;
import com.example.hanoiGo.repository.CheckpointRepository;
import com.example.hanoiGo.repository.LocationDetailRepository;
import com.example.hanoiGo.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckpointService {

    private final LocationDetailRepository locationDetailRepository;
    private final CheckpointRepository checkpointRepository;
    private final UserRepository userRepository;
    private final LocationService locationService;
    private final UserService userService;
    private final CheckpointMapper checkpointMapper;

    // Method 1: enableCheckIn - Lấy list eligible locations (tận dụng distanceValue từ getListLocation)
    public List<EnableCheckpointResponse> enableCheckIn(CheckpointRequest request) {
        // Validate user
        UserResponse userResponse = userService.getUserById(request.getUserId());
        if (userResponse == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        List<LocationListResponse> locationListResponses = locationService.getListLocation(
                request.getUserLatitude(), 
                request.getUserLongitude(), 
                null,  // tag
                false, // mostVisited
                true,  // nearest (sorted tăng dần distance)
                null   // limit 
        );

        List<EnableCheckpointResponse> enableCheckpoints = new ArrayList<>();

        for (LocationListResponse locResponse : locationListResponses) {
            LocationResponse locationRes = locResponse.getLocationResponse();
            String locName = locationRes.getName();
            int distanceValue = locResponse.getDistanceValue();  

            String locId = locationDetailRepository.findIdByName(locName).orElse(null);

            if (locId == null) {
                continue;  // Skip nếu không có ID
            }

            // Check user đã checkpoint chưa
            boolean alreadyChecked = checkpointRepository.existsByUserIdAndLocationId(request.getUserId(), locId);
            if (alreadyChecked) {
                continue;  // Skip nếu đã check
            }

            // Filter chỉ <= 100m
            if (distanceValue <= 100) {
                enableCheckpoints.add(new EnableCheckpointResponse(locId, locName, distanceValue));
            } else {
                // Break sớm vì list đã sorted nearest
                break;
            }
        }

        // Nếu không có eligible, throw exception 
        if (enableCheckpoints.isEmpty()) {
            throw new RuntimeException("No eligible check-in locations within 100m.");
        }

        return enableCheckpoints;
    }

    // Method 2: checkIn - Check-in cho location cụ thể
    public CheckpointResponse checkIn(CheckpointRequest request) {
        // Validate user
        UserResponse userResponse = userService.getUserById(request.getUserId());
        if (userResponse == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        // Gọi enableCheckIn để lấy list eligible
        List<EnableCheckpointResponse> eligibleList = enableCheckIn(request);
        System.err.println("Eligible locations: " + eligibleList.size());
        for (EnableCheckpointResponse e : eligibleList) {
            System.err.println(" - " + e.getLocationName() + " (" + e.getLocationId() + ") at " + e.getDistanceValue() + "m");
        }

        // Verify locationId có trong eligible list
        String locationId = request.getLocationId();
        boolean isEligible = eligibleList.stream()
                .anyMatch(eligible -> eligible.getLocationId().equals(locationId));

        if (!isEligible) {
            throw new RuntimeException("Location is not eligible for check-in (too far or already checked).");
        }

        // Lấy location từ ID
        LocationDetail loc = locationDetailRepository.findById(locationId)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_EXISTED));

        // Tạo checkpoint 
        Checkpoint checkpoint = new Checkpoint();
        checkpoint.setUser(userRepository.findUserById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
        checkpoint.setLocation(loc);
        checkpoint.setCheckedInTime(LocalDateTime.now());
        checkpointRepository.save(checkpoint);

        // Tăng point
        User userEntity = userRepository.findUserById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userEntity.setPoints(userEntity.getPoints() + 3);
        userRepository.save(userEntity);

        // Lấy updated user response
        UserResponse updatedUserResponse = userService.getUserById(request.getUserId());

        LocationResponse locationRes = new LocationResponse();
        locationRes.setName(loc.getName()); 
        
        CheckpointResponse resp = checkpointMapper.toCheckpointResponse(checkpoint, locationRes, updatedUserResponse);

        return resp;
    }
}