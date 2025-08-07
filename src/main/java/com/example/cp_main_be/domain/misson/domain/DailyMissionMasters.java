package com.example.cp_main_be.domain.misson.domain;


import com.example.cp_main_be.domain.misson.MissionType;
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
public class DailyMissionMasters {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_master_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_type")
    private MissionType missionType;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "content")
    private String content;

    @Column(name = "reward_points")
    private Long rewardPoints;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}
