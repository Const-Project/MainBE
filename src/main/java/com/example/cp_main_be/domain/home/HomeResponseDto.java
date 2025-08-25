package com.example.cp_main_be.domain.home;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeResponseDto {

  private final UserInfo userInfo;
  private final GardenInfo gardenInfo;
  private final List<MissionInfo> todayMissions;
  private final ActivityInfo activityInfo;

  @Getter
  @Builder
  public static class UserInfo {
    private String username; // [수정] nickname -> username
    private int level;
    private long currentExp; // [수정] int -> long (User 엔티티의 experience 타입과 일치)
    private long requiredExpForNextLevel; // [수정] int -> long
    private int unreadNotificationCount;
  }

  @Getter
  @Builder
  public static class GardenInfo {
    private String plantName;
    private String plantImageUrl;
    private int waterCount;
    private int maxWaterCount;
    private int sunlightCount;
    private int maxSunlightCount;
    private String backgroundImageUrl;
    private AvatarInfo avatar; // 아바타 정보를 정원 정보의 일부로 포함
  }

  @Getter
  @Builder
  public static class AvatarInfo {
    private String characterImageUrl;
  }

  @Getter
  @Builder
  public static class MissionInfo {
    private String missionTitle;
    private String missionType; // "QUIZ", "IMAGE_DIARY" 등
    private boolean isCompleted;
  }

  @Getter
  @Builder
  public static class ActivityInfo {
    // 최근 7일간의 활동 기록 (true: 완료, false: 미완료)
    private List<Boolean> weeklyMissionStatus;
  }
}
