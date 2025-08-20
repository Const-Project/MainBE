package com.example.cp_main_be.domain.admin.dto;

import com.example.cp_main_be.domain.mission.daily_mission_masters.MissionType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

public class AdminResponseDTO {
  @Getter
  @Builder
  public static class DailyMissionMastersResDTO {
    private Long id;
    private MissionType missionType;
    private String title; // 미션 타이틀
    private String description; // 미션 내용
    private String content; // 퀴즈 내용
    private Long rewardPoints;
    private LocalDateTime createdAt;
  }

  @Getter
  @Builder
  public static class PlantMasterResDTO {
    private Long id;
    private String plantType;
    private String plantName;
    private String description;
    private String imageUrl;
    private int growthStages;
    private int unlockLevel;
    private LocalDateTime createdAt;
  }
}
