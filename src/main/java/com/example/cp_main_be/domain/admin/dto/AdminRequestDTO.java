package com.example.cp_main_be.domain.admin.dto;

import com.example.cp_main_be.domain.mission.daily_mission_master.MissionType;
import com.example.cp_main_be.domain.user.domain.UserStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class AdminRequestDTO {

  @Getter
  @Setter
  @Builder
  public static class CreateMissionRequestDTO {
    @NotNull private MissionType missionType;

    @NotNull private String title;

    @NotNull private String description;

    @NotNull private String content;

    @NotNull private Long rewardPoints;

    private Boolean isActive;
  }

  @Getter
  @Setter
  @Builder
  public static class UpdateMissionRequestDTO {
    private MissionType missionType;
    private String title;
    private String description;
    private String content;
    private Long rewardPoints;
    private Boolean isActive;
  }

  @Getter
  @Setter
  @Builder
  public static class CreateKeywordRequestDTO {
    private String keyword;
    private LocalDateTime keywordDate;
  }

  @Getter
  @Setter
  @Builder
  public static class ChangeUserStatusRequestDTO {
    @NotNull private UserStatus userStatus;
  }

  @Getter
  @Setter
  @Builder
  public static class CreateQuizRequestDTO {

    @NotNull private String optionText;

    @NotNull private boolean isCorrect;

    @NotNull private int optionOrder;

    @NotNull private Long missionMasterId;
  }

  @Getter
  @Setter
  @Builder
  public static class CreatePlantMasterRequestDTO {
    @NotNull private String plantName;
    @NotNull private String plantType;
    @NotNull private String description;
    @NotNull private String imageUrl;
  }

  @Getter
  @Setter
  @Builder
  public static class UpdatePlantMasterRequestDTO {
    private String plantName;
    private String plantType;
    private String description;
    private String imageUrl;
  }
}
