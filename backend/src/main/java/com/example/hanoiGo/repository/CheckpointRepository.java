package com.example.hanoiGo.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hanoiGo.model.Checkpoint;

public interface CheckpointRepository extends JpaRepository<Checkpoint, UUID> {
    List<Checkpoint> findByUserId(UUID userId);
    List<Checkpoint> findByLocationId(String locationId);
}