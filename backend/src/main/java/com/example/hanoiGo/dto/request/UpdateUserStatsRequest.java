package com.example.hanoiGo.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.Data;

@Getter
@Setter
@Data
public class UpdateUserStatsRequest {
    private String userId;
    private String field;
    private int newValue;
}
