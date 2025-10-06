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
  }