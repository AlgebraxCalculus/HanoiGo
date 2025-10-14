package com.example.hanoiGo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.hanoiGo.dto.response.CheckpointResponse;
import com.example.hanoiGo.dto.response.LocationResponse;
import com.example.hanoiGo.dto.response.UserResponse;
import com.example.hanoiGo.model.Checkpoint;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CheckpointMapper {
    @Mapping(target = "locationAddress", source = "locationResponse.address")
    @Mapping(target = "userName", source = "userResponse.username")
    @Mapping(target = "checkedInTime", source = "checkpoint.checkedInTime")
    @Mapping(target = "userPoint", source = "userResponse.points")
    CheckpointResponse toCheckpointResponse(Checkpoint checkpoint, LocationResponse locationResponse, UserResponse userResponse);
}
