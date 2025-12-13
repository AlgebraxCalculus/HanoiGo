package com.example.hanoiGo.repository;

import com.example.hanoiGo.model.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import java.util.UUID;
import java.util.List;

public interface UserLikeRepository extends JpaRepository<UserLike, UUID> {

    @Query("SELECT COUNT(ul) FROM UserLike ul WHERE ul.review.id = :reviewId")
    long countByReviewId(@Param("reviewId") UUID reviewId);

    @Query("SELECT ul FROM UserLike ul WHERE ul.user.id = :userId AND ul.review.id = :reviewId")
    UserLike findByUserIdAndReviewId(@Param("userId") UUID userId,
                                               @Param("reviewId") UUID reviewId);

    @Query("SELECT ul FROM UserLike ul WHERE ul.review.id = :reviewId")
    List<UserLike> findAllByReviewId(@Param("reviewId") UUID reviewId);
}
