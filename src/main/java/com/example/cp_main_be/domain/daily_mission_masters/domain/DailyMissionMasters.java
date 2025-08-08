package com.example.cp_main_be.domain.daily_mission_masters.domain;


import com.example.cp_main_be.domain.admin.dto.AdminRequestDTO;
import com.example.cp_main_be.domain.daily_mission_masters.MissionType;
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


    public void update(AdminRequestDTO.UpdateRequestDTO requestDTO) {
        if (requestDTO.getTitle() != null) {
            this.title = requestDTO.getTitle();
        }
        if (requestDTO.getDescription() != null) {
            this.description = requestDTO.getDescription();
        }
        if (requestDTO.getContent() != null) {
            this.content = requestDTO.getContent();
        }
        if (requestDTO.getMissionType() != null) {
            this.missionType = requestDTO.getMissionType();
        }
        if (requestDTO.getRewardPoints() != null) {
            this.rewardPoints = requestDTO.getRewardPoints();
        }
        if(this.isActive != null) {
            this.isActive = requestDTO.getIsActive();
        }
    }
}
