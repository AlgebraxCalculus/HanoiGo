package com.example.hanoiGo.repository;

import com.example.hanoiGo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    // Tìm user theo username
    Optional<User> findByUsername(String username);
    
    // Tìm user theo email
    Optional<User> findByEmail(String email);
    
    // Kiểm tra username đã tồn tại chưa
    boolean existsByUsername(String username);
    
    // Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);
    
    // Tìm user theo Firebase UID
    Optional<User> findByFirebaseUid(String firebaseUid);
    
    // Kiểm tra Firebase UID đã tồn tại chưa
    boolean existsByFirebaseUid(String firebaseUid);
}
