package com.example.hanoiGo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.UUID;

@Entity
@Table(name = "location_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDetail {
    
    @Id
    @Column(nullable = false)
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String description;

    @Column(name = "default_picture", nullable = false)
    private String defaultPicture;
}
