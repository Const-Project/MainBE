package com.example.cp_main_be.domain.home;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeResponseDto {

  private final UserInfo userInfo;
  private final List<GardenSummaryInfo> gardenSummaries; // 본인의 모든 정원 목록
  private final List<MissionInfo> todayMissions;

  //  private final ActivityInfo activityInfo; // 주간 미션 상태

  // --- 내부 DTO 클래스들 ---

  @Getter
  @Builder
  public static class UserInfo {
    private Long id;
    private String username;
    private int level;
    private long currentExp;
    private long requiredExpForNextLevel;
    private int unreadNotificationCount; // 읽지 않은 알림 수 (새 메시지 1)
  }

  // 각 정원의 요약 정보 (홈 화면 슬라이드에 사용)
  @Getter
  @Builder
  public static class GardenSummaryInfo {
    private Long gardenId; // 정원 ID 추가
    private Integer gardenSlotNumber;
    private AvatarInfo avatar; // 각 정원에 배치된 아바타 정보 -> 해금안되면 null
    private boolean isLocked;
    private boolean isOwnerWateringAble; // 본인 정원에 물주기 가능한지 여부 -> 해금안되면 null
    private boolean isOwnerSunlightAble; // 본인 정원에 햇빛 주기 가능한지 여부 -> 해금안되면 null
  }

  @Getter
  @Builder
  public static class AvatarInfo {
    private Long avatarId;
    private String avatarName; // 아바타 마스터 이름 (식물 이름)
    private String avatarImageUrl;
  }

  @Getter
  @Builder
  public static class MissionInfo {
    private Long missionId; // 미션 ID 추가 (프론트엔드에서 미션 클릭 시 사용)
    private String missionTitle;
    private String missionType;
    private boolean isCompleted;
    // private String description; // 필요시 미션 상세 설명 추가 가능
  }

  //  @Getter
  //  @Builder
  //  public static class ActivityInfo {
  //    private List<Boolean> weeklyMissionStatus; // 최근 7일간의 미션 완료 여부
  //  }
}
