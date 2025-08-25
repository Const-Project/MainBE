package com.example.cp_main_be.domain.admin.dto;

import com.example.cp_main_be.domain.mission.daily_mission_master.MissionType;
import com.example.cp_main_be.domain.reports.domain.ReportReason;
import com.example.cp_main_be.domain.reports.enums.ReportStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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
  public static class CreateQuizResponseDTO {
    private Long quizId;
    private String quizQuestion;
    private Long answerNumber;
    private List<QuizOptionsResponseDTO> quizOptions;
    private DailyMissionMastersResDTO dailyMissionMaster;
  }

  @Getter
  @Builder
  public static class QuizOptionsResponseDTO {
    private Long optionId;
    private String optionText;
    private int optionOrder;
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

  @Getter
  @Builder
  public static class UserResDTO {
    private Long id;
    private UUID uuid;
    private String username;
  }

  @Getter
  @Builder
  public static class ReportResDTO {
    private Long reportId;
    private ReportStatus status;
    private ReportReason reportReason;
    private LocalDateTime reportDate;
    private Long reviewerId;
    private LocalDateTime reviewDate;
  }
}
