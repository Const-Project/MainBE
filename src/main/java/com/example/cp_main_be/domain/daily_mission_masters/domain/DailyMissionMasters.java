package com.example.cp_main_be.domain.daily_mission_masters.domain;

import com.example.cp_main_be.domain.admin.dto.AdminRequestDTO;
import com.example.cp_main_be.domain.admin.dto.AdminResponseDTO;
import com.example.cp_main_be.domain.daily_mission_masters.MissionType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
  private String title; // 미션 타이틀

  @Column(name = "description")
  private String description; // 미션 내용

  @Column(name = "content")
  private String content; // 퀴즈 내용

  @Column(name = "reward_points")
  private Long rewardPoints;

  @Column(name = "is_active")
  private Boolean isActive;

  @Column(name = "created_at")
  @CreationTimestamp
  private LocalDateTime createdAt;

  public void update(AdminRequestDTO.UpdateMissionRequestDTO requestDTO) {
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
    if (this.isActive != null) {
      this.isActive = requestDTO.getIsActive();
    }
  }

  public static AdminResponseDTO.DailyMissionMastersResDTO toDailyMissionMastersResDTO(
      DailyMissionMasters dailyMissionMasters) {
    return AdminResponseDTO.DailyMissionMastersResDTO.builder()
        .id(dailyMissionMasters.getId())
        .missionType(dailyMissionMasters.getMissionType())
        .title(dailyMissionMasters.getTitle())
        .description(dailyMissionMasters.getDescription())
        .content(dailyMissionMasters.getContent())
        .rewardPoints(dailyMissionMasters.getRewardPoints())
        .createdAt(dailyMissionMasters.getCreatedAt())
        .build();
  }
}
