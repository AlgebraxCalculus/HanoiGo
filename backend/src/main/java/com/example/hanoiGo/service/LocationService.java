package com.example.hanoiGo.service;

import com.example.hanoiGo.model.LocationTag;
import com.example.hanoiGo.model.LocationDetail;
import com.example.hanoiGo.dto.response.LocationResponse;
import com.example.hanoiGo.mapper.LocationMapper;
import com.example.hanoiGo.repository.LocationDetailRepository;
import com.example.hanoiGo.repository.LocationTagRepository;
import com.example.hanoiGo.exception.ErrorCode;
import com.example.hanoiGo.exception.AppException;
import com.example.hanoiGo.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationTagRepository locationTagRepository;
    private final LocationDetailRepository locationDetailRepository;
    private final LocationMapper locationMapper;

    @Value("${goong.api.key}")
    private String goongApiKey;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<String> getTagListByLocationID (String locationId) {
        List<LocationTag> locationTags = locationTagRepository.findByLocationId(locationId);
        // if (locationTags.isEmpty()) {
        //     throw new AppException(ErrorCode.LOCATION_NOT_EXISTED);
        // }

        List<String> tagNames = new ArrayList<>();
        for (LocationTag locationTag : locationTags) {
            tagNames.add(locationTag.getTag().getName());
        }
        return tagNames;
    }

    public LocationResponse getLocationDetailById (String locationId, Float userLat, Float userLng) {
        Optional<LocationDetail> locationDetail = locationDetailRepository.findById(locationId);
        // if (locationDetail.isEmpty()) {
        //     throw new AppException(ErrorCode.LOCATION_NOT_EXISTED);
        // }

        LocationResponse locationResponse = new LocationResponse();
        locationResponse = locationMapper.toLocationResponse(locationDetail.get());

        // Gọi đến getTagsByLocationId để lấy tags
        locationResponse.setTags(getTagListByLocationID(locationId));

        // gọi đến api goong place_detail để lấy address, latitude, longitude
            try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://rsapi.goong.io/v2/place/detail")
                    .queryParam("api_key", goongApiKey)
                    .queryParam("place_id", locationId)  // bạn có thể thay locationId bằng place_id tương ứng
                    .toUriString();

            // Gọi API và nhận JSON trả về dưới dạng Map
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "OK".equals(response.get("status"))) {
                Map<String, Object> result = (Map<String, Object>) response.get("result");
                if (result != null) {
                    locationResponse.setAddress((String) result.get("formatted_address"));

                    Map<String, Object> geometry = (Map<String, Object>) result.get("geometry");
                    if (geometry != null) {
                        Map<String, Object> location = (Map<String, Object>) geometry.get("location");
                        if (location != null) {
                            Double lat = (Double) location.get("lat");
                            Double lng = (Double) location.get("lng");
                            locationResponse.setLatitude(lat);
                            locationResponse.setLongitude(lng);
                        }
                    }
                }
            } else {
                System.out.println("Goong API error or invalid response");
            }

        } catch (Exception e) {
            System.err.println("Error calling Goong API: " + e.getMessage());
        }



        //gọi đến Goong distance_matrix để lấy distance
        if(userLat != null && userLng != null) { 
            try {
                String url = UriComponentsBuilder
                        .fromHttpUrl("https://rsapi.goong.io/v2/distancematrix")
                        .queryParam("api_key", goongApiKey)
                        .queryParam("origins", userLat + "," + userLng)
                        .queryParam("destinations", locationResponse.getLatitude() + "," + locationResponse.getLongitude())
                        .toUriString();

                // Gọi API và nhận JSON trả về dưới dạng Map
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response != null) {
                    List<Map<String, Object>> rows = (List<Map<String, Object>>) response.get("rows");
                    if (rows != null && !rows.isEmpty()) {
                        Map<String, Object> firstRow = rows.get(0);

                        List<Map<String, Object>> elements = (List<Map<String, Object>>) firstRow.get("elements");
                        if (elements != null && !elements.isEmpty()) {
                            Map<String, Object> firstElement = elements.get(0);

                            Map<String, Object> distance = (Map<String, Object>) firstElement.get("distance");
                            if(distance != null) {
                                locationResponse.setDistance((String) distance.get("text"));
                                locationResponse.setDistanceValue((int) distance.get("value"));
                            }
                        }
                    }
                } else {
                    System.out.println("Goong API error or invalid response");
                }

            } catch (Exception e) {
                System.err.println("Error calling Goong API: " + e.getMessage());
            }
        }

        return locationResponse;
    }
  }