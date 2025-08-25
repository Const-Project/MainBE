package com.example.cp_main_be.domain.admin.dto;

import com.example.cp_main_be.domain.member.user.domain.UserStatus;
import com.example.cp_main_be.domain.mission.daily_mission_master.MissionType;
import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

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
  public static class CreateQuizOptionRequestDTO {

    @NotNull private String optionText;
    @NotNull private int optionOrder;
  }

  @Getter
  @Setter
  @Builder
  public static class CreateQuizRequestDTO {
    private CreateMissionRequestDTO forCreateMission; // 미션 마스터 생성을 위한 요청
    private String quizQuestion;
    private QuizType quizType;
    private Long answerNumber;
    private List<CreateQuizOptionRequestDTO> quizOptions;
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
