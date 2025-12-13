package com.example.hanoiGo.repository;

import com.example.hanoiGo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @Query("SELECT r FROM Review r WHERE r.user.id = :userId AND r.location.id = :locationId")
    Optional<Review> findByUserIdAndLocationId(@Param("userId") UUID userId,
                                               @Param("locationId") String locationId);

    @Query("SELECT r FROM Review r WHERE r.location.id = :locationId")
    List<Review> findByLocationId(@Param("locationId") String locationId);
}
