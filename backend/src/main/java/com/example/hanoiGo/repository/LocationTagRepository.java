package com.example.hanoiGo.repository;

import com.example.hanoiGo.model.LocationTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface LocationTagRepository extends JpaRepository<LocationTag, String> {

    @Query("SELECT lt FROM LocationTag lt JOIN FETCH lt.tag WHERE lt.locationId = :locationId")
    List<LocationTag> findByLocationId(@Param("locationId") String locationId);

    @Query("""
        SELECT DISTINCT lt.locationId
        FROM LocationTag lt
        JOIN lt.tag t
        WHERE t.name IN :tagNames
        """)
    List<String> findLocationIdsByTagNames(@Param("tagNames") List<String> tagNames);
}