package com.example.hanoiGo.repository;

import com.example.hanoiGo.model.LocationTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LocationTagRepository extends JpaRepository<LocationTag, String> {

    @Query("SELECT lt FROM LocationTag lt JOIN FETCH lt.tag WHERE lt.locationId = :locationId")
    List<LocationTag> findByLocationId(String locationId);
}