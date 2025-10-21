package com.example.hanoiGo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.hanoiGo.dto.response.CheckpointResponse;
import com.example.hanoiGo.dto.response.LocationResponse;
import com.example.hanoiGo.dto.response.ReviewResponse;
import com.example.hanoiGo.model.Checkpoint;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CheckpointMapper {
    @Mapping(target = "checkedInTime", source = "checkpoint.checkedInTime")
    @Mapping(target = "location", source = "locationResponse")
    @Mapping(target = "review", source = "reviewResponse")
    CheckpointResponse toCheckpointResponse(Checkpoint checkpoint, LocationResponse locationResponse, ReviewResponse reviewResponse);
}
