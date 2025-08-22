package com.example.cp_main_be.domain.mission.user_daily_mission.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MissionPanelResponse {

  private final List<DailyMissionStatusDto> dailyMissions;
  private final WishTreeDto wishTree;

  @Getter
  @Builder
  public static class DailyMissionStatusDto {
    private Long userDailyMissionId;
    private String missionType;
    private String title;
    private boolean isCompleted;
  }

  @Getter
  @Builder
  public static class WishTreeDto {
    private String currentStage;
    private int currentPoints;
    private int requiredPointsForNextStage;
  }
}
