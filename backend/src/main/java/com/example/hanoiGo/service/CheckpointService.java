package com.example.hanoiGo.service;

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
import com.example.hanoiGo.model.LocationDetail;
import com.example.hanoiGo.model.User;
import com.example.hanoiGo.repository.CheckpointRepository;
import com.example.hanoiGo.repository.LocationDetailRepository;
import com.example.hanoiGo.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${goong.api.key}")
    private String goongApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<CheckpointResponse> checkIn(CheckpointRequest request) {
        UserResponse userResponse = userService.getUserById(request.getUserId());
        if (userResponse == null) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        List<LocationDetail> locations = locationDetailRepository.findAll();
        List<CheckpointResponse> responses = new ArrayList<>();

        for (LocationDetail loc : locations) {
            // Kiểm tra user đã checkin chưa
            boolean alreadyChecked = checkpointRepository.existsByUserIdAndLocationId(request.getUserId(), loc.getId());
            if (alreadyChecked) continue; // nếu đã checkin rồi thì bỏ qua

            try {
                // Lấy distance từ Goong Distance Matrix API
                String url = UriComponentsBuilder
                        .fromUriString("https://rsapi.goong.io/v2/distancematrix")
                        .queryParam("api_key", goongApiKey)
                        .queryParam("origins",request.getUserLatitude() + "," + request.getUserLongitude())
                        .queryParam("destinations", loc.getLatitude() + "," + loc.getLongitude())
                        .queryParam("vehicle", "car")
                        .toUriString();

                Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                List<Map<String, Object>> rows = (List<Map<String, Object>>) response.get("rows");
                if (rows.isEmpty()) continue;

                Map<String, Object> elements = (Map<String, Object>) ((List<?>) rows.get(0).get("elements")).get(0);
                Map<String, Object> distance = (Map<String, Object>) elements.get("distance");
                if (distance == null) continue;

                int distanceValue = (int) distance.get("value"); // meters

                if (distanceValue <= 100) { // Trong bán kính 100m -> checkpoint
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

                    UserResponse updatedUserResponse = userService.getUserById(request.getUserId());

                    // Lấy LocationResponse để map
                    LocationResponse locationResponse = locationService.getLocationDetailById(loc.getId());

                    CheckpointResponse resp = checkpointMapper.toCheckpointResponse(checkpoint, locationResponse, updatedUserResponse);
                    responses.add(resp);
                }

            } catch (Exception e) {
                // Log lỗi nhưng không dừng toàn bộ loop
                System.err.println("Error checking location " + loc.getName() + ": " + e.getMessage());
            }
        }

        return responses;
    }
}
