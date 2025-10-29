package com.example.hanoiGo.repository;

import com.example.hanoiGo.model.LocationDetail;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface LocationDetailRepository extends JpaRepository<LocationDetail, String> {

    @Query("SELECT l.id FROM LocationDetail l")
    List<String> findAllIds();

    @Query(value = """
        SELECT 
            COALESCE(COUNT(c.id), 0) AS checkin_count
        FROM 
            location_detail l
        LEFT JOIN 
            checkpoints c 
            ON l.id = c.location_id
            AND c.checked_in_time >= NOW() - INTERVAL '7 days'
        where
            l.id = :locationId
        GROUP BY 
            l.id
        """,
        nativeQuery = true)
    Integer findWeeklyCheckinCountsById(String locationId);

    // Tìm location detail theo Id
    Optional<LocationDetail> findById(String locationId);
    
    @Query("SELECT l FROM LocationDetail l WHERE l.address = :address")
    Optional<LocationDetail> findByAddress(String address);

    List<LocationDetail> findTop10ByAddressIgnoreCaseContaining(String keyword);
}
