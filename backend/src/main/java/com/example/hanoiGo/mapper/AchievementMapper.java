package com.example.hanoiGo.mapper;

import com.example.hanoiGo.dto.response.AchievementResponse;
import com.example.hanoiGo.model.UserAchievement;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AchievementMapper {

    @Mapping(source = "achievement.name", target = "name")
    @Mapping(source = "achievement.description", target = "description")
    @Mapping(source = "achievement.tier", target = "tier")
    @Mapping(source = "earnedAt", target = "earned_at")
    AchievementResponse toAchievementResponse(UserAchievement ua);

    List<AchievementResponse> toAchievementResponseList(List<UserAchievement> achievements);
}
