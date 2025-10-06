package com.example.hanoiGo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.hanoiGo.dto.request.CheckpointRequest;
import com.example.hanoiGo.dto.response.CheckpointResponse;
import com.example.hanoiGo.dto.response.LocationResponse;
import com.example.hanoiGo.dto.response.UserResponse;
import com.example.hanoiGo.model.Checkpoint;

@Mapper(componentModel = "spring")
public interface CheckpointMapper {
    Checkpoint toCheckpoint(CheckpointRequest request);
    @Mapping(target = "locationName", source = "locationResponse.name")
    @Mapping(target = "locationAddress", source = "locationResponse.address")
    @Mapping(target = "username", source = "userResponse.username")
    CheckpointResponse toCheckpointResponse(Checkpoint checkpoint, LocationResponse locationResponse, UserResponse userResponse);
}
