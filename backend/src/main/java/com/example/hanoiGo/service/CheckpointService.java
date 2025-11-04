package com.example.hanoiGo.service;

import org.springframework.stereotype.Service;

import com.example.hanoiGo.dto.request.CheckpointRequest;
import com.example.hanoiGo.dto.response.CheckpointResponse;
import com.example.hanoiGo.dto.response.EnableCheckpointResponse;
import com.example.hanoiGo.dto.response.LocationListResponse;
import com.example.hanoiGo.dto.response.LocationResponse;
import com.example.hanoiGo.dto.response.ReviewResponse;
import com.example.hanoiGo.dto.response.UserResponse;
import com.example.hanoiGo.exception.AppException;
import com.example.hanoiGo.exception.ErrorCode;
import com.example.hanoiGo.mapper.CheckpointMapper;
import com.example.hanoiGo.mapper.LocationMapper;
import com.example.hanoiGo.model.Checkpoint;
import com.example.hanoiGo.model.LocationDetail;
import com.example.hanoiGo.model.User;
import com.example.hanoiGo.repository.CheckpointRepository;
import com.example.hanoiGo.repository.LocationDetailRepository;
import com.example.hanoiGo.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private final LocationMapper locationMapper;
    private final FirebaseService firebaseService;

    // Get list of locations eligible for check-in
    public List<EnableCheckpointResponse> enableCheckIn(UUID userId, Double userLatitude, Double userLongitude) {
        // Validate user
        UserResponse userResponse = userService.getUserById(userId);
        if (userResponse == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        List<LocationListResponse> locationListResponses = locationService.getListLocation(
                userLatitude, 
                userLongitude,
                null,  // tag
                false, // mostVisited
                true,  // nearest
                null   // limit 
        );

        List<EnableCheckpointResponse> enableCheckpoints = new ArrayList<>();

        for (LocationListResponse locRes : locationListResponses) {
            LocationResponse locationResponse = locRes.getLocationResponse();
            int distance = locRes.getDistanceValue();
            if (distance > 10000) break;

            LocationDetail detail = locationDetailRepository.findByAddress(locationResponse.getAddress()).orElse(null);
            if (detail == null) continue;

            boolean alreadyChecked = checkpointRepository.existsByUserIdAndLocationId(
                    userId, detail.getId()
            );
            if (!alreadyChecked) {
                enableCheckpoints.add(new EnableCheckpointResponse(
                        locationMapper.toLocationResponse(detail), distance
                ));
            }
        }

        // If no eligible locations found, throw exception
        if (enableCheckpoints.isEmpty()) {
            throw new RuntimeException("No eligible check-in locations within 100m.");
        }

        return enableCheckpoints;
    }

    // Check in a detail location
    public CheckpointResponse checkIn(CheckpointRequest request) {
        // Validate user (DTO)
        UserResponse userResponse = userService.getUserById(request.getUserId());
        if (userResponse == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        // Load user entity
        User userEntity = userRepository.findUserById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Load location detail by id from request
        LocationDetail loc = locationDetailRepository.findByAddress(request.getLocationAddress())
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_EXISTED));
        // Add points for check-in
        int addedPoints = 3;
        int newPoints = (userEntity.getPoints() == null ? 0 : userEntity.getPoints()) + addedPoints;
        userEntity.setPoints(newPoints);
        userRepository.save(userEntity);

        // Create and save checkpoint
        Checkpoint checkpoint = new Checkpoint();
        checkpoint.setUser(userEntity);
        checkpoint.setLocation(loc);
        checkpoint.setCheckedInTime(LocalDateTime.now());
        checkpointRepository.save(checkpoint);

        // Push updated points to Firestore userStats
        try {
            firebaseService.pushUserStatsData(userEntity.getId(), "points", newPoints);
        } catch (Exception ex) {
            System.err.println("Failed to push user stats to Firestore: " + ex.getMessage());
        }

        // Send FCM notification
        String fcm = userEntity.getFcmToken();
        if (fcm != null && !fcm.isBlank()) {
            String title = "Check-in successful!";
            String body = "You have checked in at " + loc.getAddress() + " and received +" + addedPoints + " points.";
            firebaseService.sendNotification(fcm, title, body);
        }

        // Build response objects
        LocationResponse locationRes = locationMapper.toLocationResponse(loc);
        ReviewResponse reviewRes = null; // no review on check-in
        CheckpointResponse resp = checkpointMapper.toCheckpointResponse(checkpoint, locationRes, reviewRes);
        return resp;
    }
}