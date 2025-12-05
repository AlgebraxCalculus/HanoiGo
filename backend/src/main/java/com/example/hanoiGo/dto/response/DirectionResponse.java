package com.example.hanoiGo.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class DirectionResponse {

    private String distanceText;
    private int distanceValue;

    private String durationText;
    private int durationValue;

    private String overviewPolyline;

    private List<StepInstruction> instructions;

    @Data
    public static class StepInstruction {
        private String htmlInstructions;
        private String distanceText;
        private String maneuver;
    }
}