package com.example.hanoiGo.service;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
    private final RestTemplate restTemplate = new RestTemplate();

    public CheckpointResponse checkIn(CheckpointRequest request) {
        LocationResponse locationResponse = locationService.getLocationDetailById(request.getLocationId());
        UserResponse userResponse = userService.getUserById(request.getUserId().toString());
        if (locationResponse == null) {
            throw new AppException(ErrorCode.LOCATION_NOT_EXISTED);
        }
        double locLat = locationResponse.getLatitude();
        double locLng = locationResponse.getLongitude();

        String url = UriComponentsBuilder
                .fromUriString("https://rsapi.goong.io/DistanceMatrix")
                .queryParam("origins", request.getUserLatitude() + "," + request.getUserLongitude())
                .queryParam("destinations", locLat + "," + locLng)
                .queryParam("vehicle", "car") 
                .queryParam("api_key", goongApiKey)
                .toUriString();
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null) {
            throw new AppException(ErrorCode.API_FAIL_RESPONSE);
        }

        List<Map<String, Object>> rows = (List<Map<String, Object>>) response.get("rows");
        Map<String, Object> elements = (Map<String, Object>) ((List<?>) rows.get(0).get("elements")).get(0);
        Map<String, Object> distance = (Map<String, Object>) elements.get("distance");

        int distanceValue = (int) distance.get("value"); // in meters

        double radius = 100.0; // 100 meters
        if (distanceValue > radius) {
            throw new RuntimeException("User is too far from location. Distance: " + distanceValue + "m");
        }
        Checkpoint checkpoint = checkpointMapper.toCheckpoint(request);

        try {
            checkpoint = checkpointRepository.save(checkpoint);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.CHECKPOINT_EXISTED);
        }
        return checkpointMapper.toCheckpointResponse(checkpoint, locationResponse, userResponse);
    }
}
