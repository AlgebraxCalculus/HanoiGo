package com.example.hanoiGo.controller;

import com.example.hanoiGo.dto.request.TravelPlanRequest;
import com.example.hanoiGo.dto.response.SuggestedRouteResponse;
import com.example.hanoiGo.service.TravelAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class TravelAIController {

    private final TravelAIService travelAIService;

    @PostMapping("/routes")
    public List<SuggestedRouteResponse> getSuggestedRoutes(
            @RequestBody TravelPlanRequest request
    ) {
        return travelAIService.suggestRoutes(request);
    }
}
