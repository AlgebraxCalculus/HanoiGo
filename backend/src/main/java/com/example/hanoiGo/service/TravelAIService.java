package com.example.hanoiGo.service;

import com.example.hanoiGo.dto.request.TravelPlanRequest;
import com.example.hanoiGo.dto.response.LocationResponse;
import com.example.hanoiGo.dto.response.SuggestedRouteResponse;
import com.example.hanoiGo.mapper.LocationMapper;
import com.example.hanoiGo.model.LocationDetail;
import com.example.hanoiGo.repository.LocationDetailRepository;
import com.example.hanoiGo.repository.LocationTagRepository;

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
        if (locationIds.isEmpty()) return Collections.emptyList();

        List<LocationDetail> candidates = locationDetailRepository.findAllById(locationIds);
        if (candidates.isEmpty()) return Collections.emptyList();

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

        return result.stream().distinct().collect(Collectors.toList());
    }

    private double baseScore(LocationDetail loc, List<String> tagNames) {
        String locationId = loc.getId();

        Double avgRating = locationDetailRepository.findAverageRatingByLocationId(locationId);
        double ratingScore = (avgRating != null && avgRating > 0) ? avgRating : 2.5;

        Integer reviewCount = locationDetailRepository.findReviewCountByLocationId(locationId);
        int count = (reviewCount != null) ? reviewCount : 0;
        double popularityScore = Math.min(count, 20) * 0.1; 

        Integer weeklyCheckins = locationDetailRepository.findWeeklyCheckinCountsById(locationId);
        int checkins = (weeklyCheckins != null) ? weeklyCheckins : 0;
        double checkpointScore = Math.min(checkins, 10) * 0.3; 

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

        String title = places.get(0).getName() + " - " + places.get(places.size() - 1).getName();

        String description = "Suggested " + request.getDurationDays() + " day route: "
                + places.stream()
                .map(LocationDetail::getName)
                .collect(Collectors.joining(" → "));

        double distanceKm = estimateRouteDistanceKm(places);

        String duration = estimateDurationWithUser(request, places);

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
        if (places == null || places.size() < 2) return 0.0;

        double total = 0.0;
        for (int i = 0; i < places.size() - 1; i++) {
            total += simpleDistance(
                    places.get(i).getLatitude(), places.get(i).getLongitude(),
                    places.get(i + 1).getLatitude(), places.get(i + 1).getLongitude()
            );
        }
        return Math.round(total * 10.0) / 10.0; 
    }

    private String estimateDurationWithUser(TravelPlanRequest request, List<LocationDetail> places) {
        if (places == null || places.isEmpty()) return "0m";

        double totalKm = 0.0;

        // 1) user -> first stop
        double userLat = (request != null) ? request.getUserLat() : 0.0;
        double userLng = (request != null) ? request.getUserLng() : 0.0;

        if (userLat != 0.0 && userLng != 0.0) {
            LocationDetail first = places.get(0);
            totalKm += simpleDistance(userLat, userLng, first.getLatitude(), first.getLongitude());
        }

        for (int i = 0; i < places.size() - 1; i++) {
            totalKm += simpleDistance(
                    places.get(i).getLatitude(), places.get(i).getLongitude(),
                    places.get(i + 1).getLatitude(), places.get(i + 1).getLongitude()
            );
        }

        double avgSpeedKmh = 10.0;

        int totalMinutes = (int) Math.round((totalKm / avgSpeedKmh) * 60.0);
        int h = totalMinutes / 60;
        int m = totalMinutes % 60;

        if (h <= 0) return m + "m";
        return h + "h " + m + "m";
    }

    private double simpleDistance(double lat1, double lon1, double lat2, double lon2) {
        double dx = lat1 - lat2;
        double dy = lon1 - lon2;
        return Math.sqrt(dx * dx + dy * dy) * 111;
    }
}
