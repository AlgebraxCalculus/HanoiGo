package com.example.hanoiGo.service;

import com.example.hanoiGo.dto.response.AchievementResponse;
import com.example.hanoiGo.mapper.AchievementMapper;
import com.example.hanoiGo.model.UserAchievement;
import com.example.hanoiGo.model.User;
import com.example.hanoiGo.repository.UserRepository;
import com.example.hanoiGo.exception.ErrorCode;
import com.example.hanoiGo.exception.AppException;
import com.example.hanoiGo.repository.UserAchievementRepository;
import com.example.hanoiGo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final UserAchievementRepository userAchievementRepository;
    private final JwtUtil jwtUtil;
    private final AchievementMapper achievementMapper;
    private final UserRepository userRepository;

    public Integer getTotalAchievementsForCurrentUser(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);    
        }
        UUID userId = userOpt.get().getId();
        return userAchievementRepository.countByUserId(userId);
    }

    public List<AchievementResponse> getAchievementsForCurrentUser(String jwtToken, String tierSort, String earnedAtSort) {
        String username = jwtUtil.getUsernameFromToken(jwtToken);
        System.out.println("Username from token: " + username);
        Optional<User> userOpt = userRepository.findByUsername(username);
         if (userOpt.isEmpty()) {
             throw new AppException(ErrorCode.USER_NOT_EXISTED);    
         }
        UUID userId = userOpt.get().getId();
        List<UserAchievement> achievements = userAchievementRepository.findByUserId(userId);

        achievements = sortAchievements(achievements, tierSort, earnedAtSort);
        
        return achievementMapper.toAchievementResponseList(achievements);

    }

        // Map tier sang rank số
    private int getTierRank(String tier) {
        return switch (tier) {
            case "A" -> 1;
            case "S" -> 2;
            case "S+" -> 3;
            case "SS" -> 4;
            case "SSS" -> 5;
            default -> 0; // nếu có tier lạ
        };
    }

    // Comparator chính
    public List<UserAchievement> sortAchievements(
            List<UserAchievement> achievements, 
            String tierSort, 
            String earnedAtSort
    ) {
        if ((tierSort != null && !tierSort.isEmpty()) &&
            (earnedAtSort != null && !earnedAtSort.isEmpty())) {

            achievements.sort(
                Comparator
                    .comparing((UserAchievement ua) -> getTierRank(ua.getAchievement().getTier()),
                            getSortOrder(tierSort))
                    .thenComparing(UserAchievement::getEarnedAt, getSortOrder(earnedAtSort))
            );

        } else if (tierSort != null && !tierSort.isEmpty()) {

            achievements.sort(
                Comparator.comparing(
                    (UserAchievement ua) -> getTierRank(ua.getAchievement().getTier()),
                    getSortOrder(tierSort))
            );

        } else if (earnedAtSort != null && !earnedAtSort.isEmpty()) {

            achievements.sort(
                Comparator.comparing(UserAchievement::getEarnedAt, getSortOrder(earnedAtSort))
            );
        }

        return achievements;
    }

    //Hàm getSortOrder() của bạn trả về Comparator<Object>, trong khi Comparator.comparing() lại 
    // mong đợi kiểu cụ thể (ví dụ Comparator<Integer> hoặc Comparator<LocalDateTime>)

    //=> Cần đổi kiểu trả về của getSortOrder() thành Comparator<T> (generic), thay vì Comparator<Object>.
    private <T extends Comparable<? super T>> Comparator<T> getSortOrder(String sortParam) {
        if ("desc".equalsIgnoreCase(sortParam)) {
            return Comparator.reverseOrder();
        }
        return Comparator.naturalOrder();
    }

}