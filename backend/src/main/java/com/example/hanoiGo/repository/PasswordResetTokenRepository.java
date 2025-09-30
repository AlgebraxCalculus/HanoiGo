package com.example.hanoiGo.repository;

import com.example.hanoiGo.model.PasswordResetToken;
import com.example.hanoiGo.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    PasswordResetToken findByToken(String token);

    PasswordResetToken findByUser(User user);

    PasswordResetToken findFirstByVerifiedTrueOrderByExpiryDateDesc();
}