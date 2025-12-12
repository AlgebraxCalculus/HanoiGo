package com.example.hanoiGo.repository;

import com.example.hanoiGo.model.LocationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationDetailRepository extends JpaRepository<LocationDetail, String> {

    /**
     * Lấy tất cả id của location_detail (dùng khi cần random / iterate toàn bộ).
     */
    @Query("SELECT l.id FROM LocationDetail l")
    List<String> findAllIds();

    /**
     * Đếm số lần check-in trong 7 ngày gần nhất cho 1 địa điểm.
     * Trả về số checkpoint (Integer), có thể null nếu không có bản ghi.
     */
    @Query(value = """
        SELECT 
            COALESCE(COUNT(c.id), 0) AS checkin_count
        FROM 
            location_detail l
        LEFT JOIN 
            checkpoints c 
            ON l.id = c.location_id
            AND c.checked_in_time >= NOW() - INTERVAL '7 days'
        WHERE
            l.id = :locationId
        GROUP BY 
            l.id
        """,
        nativeQuery = true)
    Integer findWeeklyCheckinCountsById(@Param("locationId") String locationId);

    /**
     * Tìm location theo address chính xác.
     */
    @Query("SELECT l FROM LocationDetail l WHERE l.address = :address")
    Optional<LocationDetail> findByAddress(@Param("address") String address);

    /**
     * Tìm top 10 địa chỉ có chứa keyword (không phân biệt hoa thường).
     */
    List<LocationDetail> findTop10ByAddressIgnoreCaseContaining(String keyword);

    /**
     * Điểm rating trung bình của 1 location dựa trên bảng reviews.
     * Nếu không có review nào -> trả về 0 (nhờ COALESCE).
     */
    @Query(value = """
        SELECT COALESCE(AVG(r.rating), 0)
        FROM reviews r
        WHERE r.location_id = :locationId
        """,
        nativeQuery = true)
    Double findAverageRatingByLocationId(@Param("locationId") String locationId);

    /**
     * Số lượng review của 1 location trong bảng reviews.
     */
    @Query(value = """
        SELECT COUNT(r.id)
        FROM reviews r
        WHERE r.location_id = :locationId
        """,
        nativeQuery = true)
    Integer findReviewCountByLocationId(@Param("locationId") String locationId);
}
