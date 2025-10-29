package com.example.hanoiGo.service;

import com.example.hanoiGo.model.LocationTag;
import com.example.hanoiGo.model.LocationDetail;
import com.example.hanoiGo.dto.response.LocationResponse;
import com.example.hanoiGo.dto.response.LocationListResponse;
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
import java.util.Iterator;
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

    @Value("${goong.api.key}")  // Lấy giá trị từ application.properties
    private String GOONG_API_KEY;
    private final RestTemplate restTemplate = new RestTemplate();

    // Lấy danh sách tag theo locationId
    public List<String> getTagListByLocationID(String locationId) {
        List<LocationTag> locationTags = locationTagRepository.findByLocationId(locationId);
        List<String> tagNames = new ArrayList<>();
        for (LocationTag locationTag : locationTags) {
            tagNames.add(locationTag.getTag().getName());
        }
        return tagNames;
    }

    // Lấy chi tiết location theo ID
    public LocationResponse getLocationDetailById(String locationId) {
        LocationDetail locationDetail = locationDetailRepository.findById(locationId)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_EXISTED));

        // Map entity sang DTO
        LocationResponse locationResponse = locationMapper.toLocationResponse(locationDetail);

        // Lấy tags từ DB
        locationResponse.setTags(getTagListByLocationID(locationId));

        return locationResponse;
    }

    // Lấy danh sách location theo ID, lat, lng và các filter
    public List<LocationListResponse> getListLocation(Double lat, Double lng, String tag, Boolean mostVisited, Boolean nearest, Integer limit) {
        List<String> locationIds = locationDetailRepository.findAllIds();
        List<LocationListResponse> locationList = new ArrayList<>();
        String ok = "0";

        // TH: lọc theo tag hoặc mostVisited
        if(tag != null && !tag.isEmpty()) {
            ok = "1";
            locationIds.removeIf(lcId -> !getTagListByLocationID(lcId).contains(tag));
        } else if(mostVisited != null && mostVisited){
            ok = "1";
            locationIds.sort((id1, id2) -> {
                Integer count1 = locationDetailRepository.findWeeklyCheckinCountsById(id1);
                Integer count2 = locationDetailRepository.findWeeklyCheckinCountsById(id2);
                return count2.compareTo(count1); // Sắp xếp giảm dần
            });
        }
        // Lấy thông tin location và khoảng cách từ API Goong
        for(String lcIds : locationIds) {
            LocationListResponse locationListResponse = new LocationListResponse();
            locationListResponse.setLocationResponse(getLocationDetailById(lcIds));
            locationList.add(locationListResponse);
        }
        if (locationList.isEmpty()) {
            throw new AppException(ErrorCode.LOCATION_NOT_EXISTED);
        }

        String destinations = "";
        for(LocationListResponse llr : locationList) {
            destinations += llr.getLocationResponse().getLatitude() + "," + llr.getLocationResponse().getLongitude() + "|";
        }
        destinations = destinations.substring(0, destinations.length() - 1);

        // Gọi api goong distancematrix với tham số api_key, origins, destinations
        String url = UriComponentsBuilder.fromHttpUrl("https://rsapi.goong.io/v2/distancematrix")
                .queryParam("api_key", GOONG_API_KEY)
                .queryParam("origins", lat + "," + lng)
                .queryParam("destinations", destinations)
                .toUriString();
        url = url.replace("%7C", "|");
        try {
            // Gọi API
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.get("rows") instanceof List<?> rowsList && !rowsList.isEmpty()) {
                Map<String, Object> row = (Map<String, Object>) rowsList.get(0);
                if (row.get("elements") instanceof List<?> elementsList) {
                    List<?> elements = elementsList;
                    for (int i = 0; i < elements.size(); i++) {
                        Map<String, Object> element = (Map<String, Object>) elements.get(i);
                        if (element.get("distance") instanceof Map<?, ?> distanceMap) {
                            Map<String, Object> distance = (Map<String, Object>) distanceMap;
                            locationList.get(i).setDistanceText((String) distance.get("text"));
                            locationList.get(i).setDistanceValue(((Number) distance.get("value")).intValue());
                        }
                    }
                }
            } else {
                System.out.println("Goong API returned no rows/elements");
            }

        } catch (Exception e) {
            System.err.println("Error calling Goong DistanceMatrix API: " + e.getMessage());
        }            
        if(ok.equals("1")) {
            if(limit != null && limit > 0){
                limit = Math.min(limit, locationList.size());
                locationList = locationList.subList(0, limit);
            }
            return locationList;
        }
            
        // TH2: sắp xếp theo khoảng cách tăng dần nếu nearest = true
        if(nearest != null && nearest) {
            // Sắp xếp theo khoảng cách tăng dần
            locationList.sort(Comparator.comparingInt(LocationListResponse::getDistanceValue));
        }
        if(limit != null && limit > 0){
            limit = Math.min(limit, locationList.size());
            locationList = locationList.subList(0, limit);
        }
        return locationList;
    }

    // Lấy locationId theo address
    public LocationResponse getLocationDetailByAddress(String address) {
        LocationDetail locationDetail = locationDetailRepository.findByAddress(address)
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_EXISTED));

        // Map entity sang DTO
        LocationResponse locationResponse = locationMapper.toLocationResponse(locationDetail);

        // Lấy tags từ DB
        locationResponse.setTags(getTagListByLocationID(locationDetail.getId()));

        return locationResponse;
    }

    public List<LocationResponse> searchAutocompleteByAddress(String keyword) {
        List<LocationResponse> resultList = new ArrayList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return resultList;
        }

        // Tìm danh sách location có address chứa keyword
        List<LocationDetail> locations = locationDetailRepository
                .findTop10ByAddressIgnoreCaseContaining(keyword.trim());

        for (LocationDetail location : locations) {
            LocationResponse dto = locationMapper.toLocationResponse(location);
            dto.setTags(getTagListByLocationID(location.getId()));
            resultList.add(dto);
        }

        return resultList;
    }

  }