package com.example.cp_main_be.domain.mission.daily_mission_master.domain;

import com.example.cp_main_be.domain.admin.dto.AdminRequestDTO;
import com.example.cp_main_be.domain.admin.dto.AdminResponseDTO;
import com.example.cp_main_be.domain.mission.daily_mission_master.MissionType;
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
public class DailyMissionMaster {

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
      DailyMissionMaster dailyMissionMaster) {
    return AdminResponseDTO.DailyMissionMastersResDTO.builder()
        .id(dailyMissionMaster.getId())
        .missionType(dailyMissionMaster.getMissionType())
        .title(dailyMissionMaster.getTitle())
        .description(dailyMissionMaster.getDescription())
        .content(dailyMissionMaster.getContent())
        .rewardPoints(dailyMissionMaster.getRewardPoints())
        .createdAt(dailyMissionMaster.getCreatedAt())
        .build();
  }
}
