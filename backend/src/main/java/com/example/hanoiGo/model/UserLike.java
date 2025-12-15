package com.example.hanoiGo.model;

import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "user_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "review_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLike {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @CreationTimestamp
    @Column(name = "liked_at", nullable = false)
    private LocalDateTime likedAt;
}
