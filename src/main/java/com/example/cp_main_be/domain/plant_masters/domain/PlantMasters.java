package com.example.cp_main_be.domain.plant_masters.domain;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlantMasters {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plant_master_id")
    private Long id;

    @Column(name = "plant_name")
    private String plantName;

    @Column(name = "plant_type")
    private String plantType;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "growth_stages")
    private int growthStages = 0;

    @Column(name = "unlock_level")
    private int unlockLevel = 0;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

}
