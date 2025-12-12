package com.example.hanoiGo.service;

import com.example.hanoiGo.dto.request.TravelPlanRequest;
import com.example.hanoiGo.dto.response.LocationResponse;
import com.example.hanoiGo.dto.response.SuggestedRouteResponse;
import com.example.hanoiGo.model.LocationDetail;
import com.example.hanoiGo.repository.LocationDetailRepository;
import com.example.hanoiGo.repository.LocationTagRepository;
import com.example.hanoiGo.mapper.LocationMapper; 

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelAIService {

    private final LocationDetailRepository locationDetailRepository;
    private final LocationTagRepository locationTagRepository;
    private final LocationMapper locationMapper; 

    public List<SuggestedRouteResponse> suggestRoutes(TravelPlanRequest request) {

        List<String> tagNames = mapInterestsToTags(request.getInterests());

        List<String> locationIds = locationTagRepository.findLocationIdsByTagNames(tagNames);
        if (locationIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<LocationDetail> candidates = locationDetailRepository.findAllById(locationIds);
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        Map<LocationDetail, Double> scoreMap = new HashMap<>();
        for (LocationDetail loc : candidates) {
            double score = baseScore(loc, tagNames);
            scoreMap.put(loc, score);
        }

        List<LocationDetail> sorted = candidates.stream()
                .sorted((a, b) -> Double.compare(scoreMap.get(b), scoreMap.get(a)))
                .collect(Collectors.toList());

        int routeSize = 4; 
        return buildRoutes(sorted, routeSize, request);
    }

    private List<String> mapInterestsToTags(String[] interests) {
        if (interests == null || interests.length == 0) {
            return List.of("Iconic", "Cuisine", "Entertaining", "Culture");
        }

        List<String> result = new ArrayList<>();
        for (String interest : interests) {
            String normalized = interest.toLowerCase().trim();

            if (normalized.contains("ẩm thực") || normalized.contains("food")) {
                result.add("Cuisine");
            }
            if (normalized.contains("văn hóa") || normalized.contains("culture")) {
                result.add("Culture");
            }
            if (normalized.contains("giải trí") || normalized.contains("entertain")) {
                result.add("Entertaining");
            }
            if (normalized.contains("iconic") || normalized.contains("nổi bật") || normalized.contains("landmark")) {
                result.add("Iconic");
            }
        }

        if (result.isEmpty()) {
            result.addAll(List.of("Iconic", "Cuisine", "Entertaining", "Culture"));
        }

        // bỏ trùng
        return result.stream().distinct().collect(Collectors.toList());
    }
    /**
     * Score 1 địa điểm:
     * = điểm rating trung bình (1–5, nếu chưa có review => 2.5)
     * + điểm theo số lượng review (mỗi review +0.1, tối đa 20 review => +2.0)
     * + điểm theo checkpoint 7 ngày gần nhất (mỗi check-in +0.3, tối đa 10 => +3.0)
     */
    private double baseScore(LocationDetail loc, List<String> tagNames) {
        String locationId = loc.getId();

        // 1️⃣ Điểm rating trung bình
        Double avgRating = locationDetailRepository.findAverageRatingByLocationId(locationId);
        double ratingScore = (avgRating != null && avgRating > 0) ? avgRating : 2.5;

        // 2️⃣ Điểm theo số lượng review
        Integer reviewCount = locationDetailRepository.findReviewCountByLocationId(locationId);
        int count = (reviewCount != null) ? reviewCount : 0;
        double popularityScore = Math.min(count, 20) * 0.1;  // max +2.0

        // 3️⃣ Điểm theo checkpoint (độ "hot" gần đây)
        Integer weeklyCheckins = locationDetailRepository.findWeeklyCheckinCountsById(locationId);
        int checkins = (weeklyCheckins != null) ? weeklyCheckins : 0;
        double checkpointScore = Math.min(checkins, 10) * 0.3; // max +3.0

        // 4️⃣ Tổng score
        return ratingScore + popularityScore + checkpointScore;
    }


    private List<SuggestedRouteResponse> buildRoutes(List<LocationDetail> sorted,
                                                     int routeSize,
                                                     TravelPlanRequest request) {

        List<SuggestedRouteResponse> routes = new ArrayList<>();
        Set<String> usedIds = new HashSet<>();

        for (LocationDetail start : sorted) {
            if (usedIds.contains(start.getId())) continue;

            List<LocationDetail> routePlaces = new ArrayList<>();
            routePlaces.add(start);
            usedIds.add(start.getId());

            while (routePlaces.size() < routeSize) {
                LocationDetail last = routePlaces.get(routePlaces.size() - 1);

                Optional<LocationDetail> nextOpt = sorted.stream()
                        .filter(loc -> !usedIds.contains(loc.getId()))
                        .min(Comparator.comparingDouble(loc ->
                                simpleDistance(last.getLatitude(), last.getLongitude(),
                                               loc.getLatitude(), loc.getLongitude())
                        ));

                if (nextOpt.isEmpty()) break;

                LocationDetail next = nextOpt.get();
                routePlaces.add(next);
                usedIds.add(next.getId());
            }

            routes.add(buildRouteResponse(routePlaces, request));
        }

        return routes;
    }

    private SuggestedRouteResponse buildRouteResponse(List<LocationDetail> places,
                                                      TravelPlanRequest request) {

        String title = places.get(0).getName()
                + " - "
                + places.get(places.size() - 1).getName();

        String description = "Lộ trình " + request.getDurationDays() + " ngày gợi ý: "
                + places.stream()
                        .map(LocationDetail::getName)
                        .collect(Collectors.joining(" → "));

        double distanceKm = estimateRouteDistanceKm(places);
        String duration = estimateDuration(distanceKm);

        List<LocationResponse> stops = places.stream()
                .map(locationMapper::toLocationResponse)
                .collect(Collectors.toList());

        SuggestedRouteResponse res = new SuggestedRouteResponse();
        res.setTitle(title);
        res.setDescription(description);
        res.setDistanceKm(distanceKm);
        res.setDuration(duration);
        res.setStops(stops);

        return res;
    }
    private double estimateRouteDistanceKm(List<LocationDetail> places) {
        if (places.size() < 2) return 0.0;

        double total = 0.0;
        for (int i = 0; i < places.size() - 1; i++) {
            total += simpleDistance(
                    places.get(i).getLatitude(), places.get(i).getLongitude(),
                    places.get(i + 1).getLatitude(), places.get(i + 1).getLongitude()
            );
        }
        return Math.round(total * 10.0) / 10.0; 
    }

    private double simpleDistance(double lat1, double lon1, double lat2, double lon2) {
        double dx = lat1 - lat2;
        double dy = lon1 - lon2;
        return Math.sqrt(dx * dx + dy * dy) * 111;
    }

    private String estimateDuration(double distanceKm) {
        double hours = distanceKm / 5.0;
        int totalMinutes = (int) Math.round(hours * 60);

        int h = totalMinutes / 60;
        int m = totalMinutes % 60;
        if (h == 0) return m + "m";
        return h + "h " + m + "m";
    }
}
