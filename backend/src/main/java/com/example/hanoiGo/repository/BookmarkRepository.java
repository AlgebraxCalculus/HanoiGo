package com.example.hanoiGo.repository;

import com.example.hanoiGo.model.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {

    // Kiểm tra bookmark tồn tại trong bookmark list
    boolean existsByBookmarkListIdAndLocationId(UUID bookmarkListId, String locationId);

    // Tìm bookmark theo bookmark list và location
    Optional<Bookmark> findByBookmarkListIdAndLocationId(UUID bookmarkListId, String locationId);

    // Lấy tất cả bookmarks trong 1 bookmark list
    List<Bookmark> findByBookmarkListId(UUID bookmarkListId);

    // Đếm số bookmarks trong 1 bookmark list
    long countByBookmarkListId(UUID bookmarkListId);

    // Xóa bookmark theo bookmark list và location
    void deleteByBookmarkListIdAndLocationId(UUID bookmarkListId, String locationId);

    // Lấy tất cả bookmarks của user (qua tất cả bookmark lists của user)
    @Query("SELECT b FROM Bookmark b WHERE b.bookmarkList.user.id = :userId")
    List<Bookmark> findAllByUserId(@Param("userId") UUID userId);
}
