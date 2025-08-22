package com.example.cp_main_be.domain.home.service;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenRepository;
import com.example.cp_main_be.domain.home.HomeResponseDto;
import com.example.cp_main_be.domain.member.notification.domain.repository.NotificationRepository;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserDailyMission;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.repository.UserDailyMissionRepository;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

  private final UserRepository userRepository;
  private final GardenRepository gardenRepository;
  private final UserDailyMissionRepository userDailyMissionRepository;
  private final NotificationRepository notificationRepository;

  public HomeResponseDto getHomeScreenData(Long userId) {
    // 1. 핵심 엔티티 조회
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));

    Garden garden =
        gardenRepository
            .findByUserWithDetails(user)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));

    Avatar avatar = garden.getAvatar();

    // 2. 부가 정보 조회
    int unreadNotificationCount = notificationRepository.countByReceiverAndIsReadFalse(user);

    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

    List<UserDailyMission> todayMissions =
        userDailyMissionRepository.findTodayMissionsWithMasterByUser(user, startOfDay, endOfDay);

    // TODO: Repository에 주간 활동 기록 조회 로직 구현 필요
    List<Boolean> weeklyStatus = createWeeklyMissionStatus(userId);

    HomeResponseDto.ActivityInfo activityInfo =
        HomeResponseDto.ActivityInfo.builder().weeklyMissionStatus(weeklyStatus).build();

    // 3. DTO 조립
    HomeResponseDto.UserInfo userInfo =
        HomeResponseDto.UserInfo.builder()
            .username(user.getNickname())
            .level(user.getLevel())
            .currentExp(user.getExperience())
            .requiredExpForNextLevel(calculateRequiredExpForLevel(user.getLevel() + 1))
            .unreadNotificationCount(unreadNotificationCount)
            .build();

    List<HomeResponseDto.ItemInfo> equippedItems =
        avatar.getEquippedItems().stream()
            .map(
                item ->
                    HomeResponseDto.ItemInfo.builder()
                        .itemType(item.getType().getDisplayName())
                        .itemImageUrl(item.getImageUrl())
                        .build())
            .collect(Collectors.toList());

    // 또는 각각 개별적으로 가져올 수도 있습니다
    HomeResponseDto.ItemInfo hatInfo = null;
    if (avatar.getEquippedHat() != null) {
      hatInfo =
          HomeResponseDto.ItemInfo.builder()
              .itemType("HAT")
              .itemImageUrl(avatar.getEquippedHat().getImageUrl())
              .build();
    }

    HomeResponseDto.ItemInfo clothesInfo = null;
    if (avatar.getEquippedClothes() != null) {
      clothesInfo =
          HomeResponseDto.ItemInfo.builder()
              .itemType("CLOTHES")
              .itemImageUrl(avatar.getEquippedClothes().getImageUrl())
              .build();
    }

    HomeResponseDto.AvatarInfo avatarInfo =
        HomeResponseDto.AvatarInfo.builder()
            .characterImageUrl(avatar.getAvatarMaster().getDefaultImageUrl())
            .equippedItems(equippedItems)
            .build();

    HomeResponseDto.GardenInfo gardenInfo =
        HomeResponseDto.GardenInfo.builder()
            .waterCount(garden.getWaterCount())
            .maxWaterCount(100)
            .sunlightCount(garden.getSunlightCount())
            .maxSunlightCount(100)
            .backgroundImageUrl(garden.getGardenBackground().getImageUrl())
            .avatar(avatarInfo)
            .build();

    List<HomeResponseDto.MissionInfo> missionInfos =
        todayMissions.stream()
            .map(
                mission -> {
                  DailyMissionMaster master = mission.getDailyMissionMaster();
                  return HomeResponseDto.MissionInfo.builder()
                      .missionTitle(master.getTitle())
                      .missionType(master.getMissionType().toString())
                      .isCompleted(mission.isCompleted())
                      .build();
                })
            .collect(Collectors.toList());

    // 4. 최종 응답 반환
    return HomeResponseDto.builder()
        .userInfo(userInfo)
        .gardenInfo(gardenInfo)
        .todayMissions(missionInfos)
        .activityInfo(activityInfo)
        .build();
  }

  private long calculateRequiredExpForLevel(int level) {
    return level * 100L;
  }

  private List<Boolean> createWeeklyMissionStatus(Long userId) {
    // 1. 기준 날짜 설정 (오늘 포함 7일 전)
    LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();

    // 2. Repository를 통해 7일간의 모든 미션 기록을 한번에 조회
    List<UserDailyMission> recentMissions =
        userDailyMissionRepository.findByUserIdAndCreatedAtAfter(userId, sevenDaysAgo);

    // 3. '완료된' 미션들의 날짜만 Set으로 추출 (중복 제거)
    Set<LocalDate> completedDates =
        recentMissions.stream()
            .filter(UserDailyMission::isCompleted)
            .map(mission -> mission.getCreatedAt().toLocalDate())
            .collect(Collectors.toSet());

    // 4. 최근 7일(오늘-6일전 ~ 오늘)을 순회하며 완료 여부 리스트 생성
    return Stream.iterate(LocalDate.now().minusDays(6), date -> date.plusDays(1))
        .limit(7)
        .map(completedDates::contains) // 해당 날짜가 completedDates Set에 포함되어 있는지 확인
        .collect(Collectors.toList());
  }
}
