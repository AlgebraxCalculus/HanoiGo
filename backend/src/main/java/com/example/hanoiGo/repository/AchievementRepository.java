package com.example.hanoiGo.repository;

import com.example.hanoiGo.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.UUID;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, UUID> {

    // Lấy achievement theo id
    Optional<Achievement> findById(UUID id);
    
}