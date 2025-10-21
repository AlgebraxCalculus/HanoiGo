package com.example.hanoiGo.repository;

import com.example.hanoiGo.model.BookmarkList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookmarkListRepository extends JpaRepository<BookmarkList, UUID> {

    // Lấy tất cả bookmark lists của 1 user
    List<BookmarkList> findByUserId(UUID userId);

    // Tìm bookmark list theo user và tên
    Optional<BookmarkList> findByUserIdAndName(UUID userId, String name);

    // Kiểm tra user có bookmark list với tên này chưa
    boolean existsByUserIdAndName(UUID userId, String name);

    // Đếm số bookmark lists của user
    long countByUserId(UUID userId);
}
