package com.example.hanoiGo.mapper;

import com.example.hanoiGo.dto.response.AchievementResponse;
import com.example.hanoiGo.model.Achievement;
import com.example.hanoiGo.model.UserAchievement;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-07T09:53:04+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251001-1143, environment: Java 22.0.2 (Oracle Corporation)"
)
@Component
public class AchievementMapperImpl implements AchievementMapper {

    @Override
    public AchievementResponse toAchievementResponse(UserAchievement ua) {
        if ( ua == null ) {
            return null;
        }

        AchievementResponse achievementResponse = new AchievementResponse();

        achievementResponse.setName( uaAchievementName( ua ) );
        achievementResponse.setDescription( uaAchievementDescription( ua ) );
        achievementResponse.setTier( uaAchievementTier( ua ) );
        achievementResponse.setEarned_at( ua.getEarnedAt() );

        return achievementResponse;
    }

    @Override
    public List<AchievementResponse> toAchievementResponseList(List<UserAchievement> achievements) {
        if ( achievements == null ) {
            return null;
        }

        List<AchievementResponse> list = new ArrayList<AchievementResponse>( achievements.size() );
        for ( UserAchievement userAchievement : achievements ) {
            list.add( toAchievementResponse( userAchievement ) );
        }

        return list;
    }

    private String uaAchievementName(UserAchievement userAchievement) {
        if ( userAchievement == null ) {
            return null;
        }
        Achievement achievement = userAchievement.getAchievement();
        if ( achievement == null ) {
            return null;
        }
        String name = achievement.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String uaAchievementDescription(UserAchievement userAchievement) {
        if ( userAchievement == null ) {
            return null;
        }
        Achievement achievement = userAchievement.getAchievement();
        if ( achievement == null ) {
            return null;
        }
        String description = achievement.getDescription();
        if ( description == null ) {
            return null;
        }
        return description;
    }

    private String uaAchievementTier(UserAchievement userAchievement) {
        if ( userAchievement == null ) {
            return null;
        }
        Achievement achievement = userAchievement.getAchievement();
        if ( achievement == null ) {
            return null;
        }
        String tier = achievement.getTier();
        if ( tier == null ) {
            return null;
        }
        return tier;
    }
}
