package com.example.hanoiGo.repository;

import com.example.hanoiGo.model.LocationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface LocationDetailRepository extends JpaRepository<LocationDetail, String> {

    // lấy ra mọi location detail
    List<LocationDetail> findAll();

    // Tìm location detail theo Id
    Optional<LocationDetail> findById(String locationId);
}
