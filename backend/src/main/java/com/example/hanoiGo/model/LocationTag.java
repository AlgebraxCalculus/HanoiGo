package com.example.hanoiGo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.UUID;

@Entity
@Table(name = "location_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationTag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tags_id" , nullable = false)
    private Tag tag;
    
    @Column(name = "location_id", nullable = false)
    private String locationId;
}
