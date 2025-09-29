package com.example.hanoiGo.mapper;

import com.example.hanoiGo.dto.request.RegisterRequest;
import com.example.hanoiGo.dto.response.UserResponse;
import com.example.hanoiGo.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toUser(RegisterRequest request);
    UserResponse toUserResponse (User user);
}
