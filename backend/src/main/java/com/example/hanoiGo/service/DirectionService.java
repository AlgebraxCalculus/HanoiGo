package com.example.hanoiGo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.hanoiGo.dto.response.DirectionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectionService {

    @Value("${goong.api.key}")
    private String GOONG_API_KEY;

    private final RestTemplate restTemplate = new RestTemplate();

    public DirectionResponse getDirection(double originLat, double originLng,
                                          double destLat, double destLng) {

        String url = UriComponentsBuilder
                .fromHttpUrl("https://rsapi.goong.io/direction")
                .queryParam("api_key", GOONG_API_KEY)
                .queryParam("origin", originLat + "," + originLng)
                .queryParam("destination", destLat + "," + destLng)
                .queryParam("vehicle", "bike")
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !response.containsKey("routes"))
            throw new RuntimeException("Invalid Goong response");

        Map<String, Object> route = ((List<Map<String, Object>>) response.get("routes")).get(0);
        Map<String, Object> leg = ((List<Map<String, Object>>) route.get("legs")).get(0);

        DirectionResponse dto = new DirectionResponse();

        /** Distance */
        Map<String, Object> distance = (Map<String, Object>) leg.get("distance");
        dto.setDistanceText((String) distance.get("text"));
        dto.setDistanceValue(toInt(distance.get("value")));

        /** Duration */
        Map<String, Object> duration = (Map<String, Object>) leg.get("duration");
        dto.setDurationText((String) duration.get("text"));
        dto.setDurationValue(toInt(duration.get("value")));

        /** Overview polyline */
        Map<String, Object> overviewPolyline = (Map<String, Object>) route.get("overview_polyline");
        dto.setOverviewPolyline((String) overviewPolyline.get("points"));

        /** Step instructions */
        List<Map<String, Object>> steps = (List<Map<String, Object>>) leg.get("steps");

        List<DirectionResponse.StepInstruction> stepList = new ArrayList<>();

        for (Map<String, Object> step : steps) {
            DirectionResponse.StepInstruction s = new DirectionResponse.StepInstruction();

            s.setHtmlInstructions((String) step.get("html_instructions"));

            Map<String, Object> stepDist = (Map<String, Object>) step.get("distance");
            s.setDistanceText((String) stepDist.get("text"));

            s.setManeuver(step.get("maneuver") != null ? step.get("maneuver").toString() : "");

            stepList.add(s);
        }

        dto.setInstructions(stepList);

        return dto;
    }

    private int toInt(Object value) {
        return value instanceof Double ? ((Double) value).intValue() : (Integer) value;
    }
}
