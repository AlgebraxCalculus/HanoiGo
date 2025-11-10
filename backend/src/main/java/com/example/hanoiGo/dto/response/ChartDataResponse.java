package com.example.hanoiGo.dto.response;

import lombok.Data;
import java.util.*;

@Data
public class ChartDataResponse {
    private String username;
    private List<Map<String, Object>> data;
}