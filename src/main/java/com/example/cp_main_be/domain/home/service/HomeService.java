package com.example.cp_main_be.domain.home.service;

import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenRepository;
import com.example.cp_main_be.domain.home.HomeResponseDto;
import com.example.cp_main_be.domain.home.PannelResponseDTO;
import com.example.cp_main_be.domain.member.notification.domain.repository.NotificationRepository;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import com.example.cp_main_be.domain.mission.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.mission.diary.service.DiaryService;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserDailyMission;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.repository.UserDailyMissionRepository;
import com.example.cp_main_be.domain.realquiz.UserQuiz;
import com.example.cp_main_be.domain.realquiz.repository.UserQuizRepository;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// HomeService.java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

  private final UserRepository userRepository;
  private final GardenRepository gardenRepository;
  private final UserDailyMissionRepository userDailyMissionRepository;
  private final NotificationRepository notificationRepository;
  private final DiaryRepository diaryRepository;
  private final DiaryService diaryService;
  private final UserQuizRepository userQuizRepository;

  // GardenService에서 가져오거나, 공통 유틸리티로 분리하면 더 좋습니다.
  private LocalDateTime getStartOfCurrentWateringDay() {
    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    LocalDateTime todayNoon = now.toLocalDate().atTime(12, 0);
    return now.isBefore(todayNoon) ? todayNoon.minusDays(1) : todayNoon;
  }

  private LocalDateTime getStartOfCurrentSunlightDay() {
    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    LocalDateTime todaySixAM = now.toLocalDate().atTime(6, 0);
    return now.isBefore(todaySixAM) ? todaySixAM.minusDays(1) : todaySixAM;
  }

  public HomeResponseDto getHomeScreenData(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));

    // 1. UserInfo 구성
    int unreadNotificationCount = notificationRepository.countByReceiverAndIsReadFalse(user);
    HomeResponseDto.UserInfo userInfo =
        HomeResponseDto.UserInfo.builder()
            .id(user.getId())
            .username(user.getNickname())
            .level(user.getLevel())
            .currentExp(user.getExperience())
            .requiredExpForNextLevel(calculateRequiredExpForLevel(user.getLevel() + 1))
            .unreadNotificationCount(unreadNotificationCount)
            .build();

    // 2. GardenSummaries 구성 (모든 정원 순회)
    List<HomeResponseDto.GardenSummaryInfo> gardenSummaries =
        user.getGardens().stream()
            .map(
                garden -> {
                  // 각 정원의 물주기/햇빛주기 가능 여부 계산 (주인 본인이 주는 경우)
                  boolean isOwnerWateringAble =
                      garden.getLastWateredByOwnerAt() == null
                          || garden
                              .getLastWateredByOwnerAt()
                              .plusHours(8)
                              .isBefore(LocalDateTime.now());

                  boolean isOwnerSunlightAble =
                      garden.getLastSunlightReceivedAt() == null
                          || garden
                              .getLastSunlightReceivedAt()
                              .isBefore(getStartOfCurrentSunlightDay()); // 매일 06시 초기화

                  return HomeResponseDto.GardenSummaryInfo.builder()
                      .gardenId(garden.getId())
                      .gardenSlotNumber(garden.getSlotNumber())
                      .avatar(
                          HomeResponseDto.AvatarInfo.builder()
                              .avatarId(garden.getAvatar().getId())
                              .avatarName(garden.getAvatar().getNickname())
                              .avatarImageUrl(
                                  garden.getAvatar().getAvatarMaster().getDefaultImageUrl())
                              .build())
                      .isOwnerWateringAble(isOwnerWateringAble)
                      .isOwnerSunlightAble(isOwnerSunlightAble)
                      .build();
                })
            .collect(Collectors.toList());

    // 3. TodayMissions 구성
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
    List<HomeResponseDto.MissionInfo> todayMissions =
        userDailyMissionRepository
            .findTodayMissionsWithMasterByUser(user, startOfDay, endOfDay)
            .stream()
            .map(
                userDailyMission ->
                    HomeResponseDto.MissionInfo.builder()
                        .missionId(userDailyMission.getDailyMissionMaster().getId()) // 마스터 미션 ID
                        .missionTitle(userDailyMission.getDailyMissionMaster().getTitle())
                        .missionType(
                            userDailyMission.getDailyMissionMaster().getMissionType().toString())
                        .isCompleted(userDailyMission.isCompleted())
                        .build())
            .collect(Collectors.toList());

    //    // 4. ActivityInfo 구성
    //    List<Boolean> weeklyStatus = createWeeklyMissionStatus(userId);
    //    HomeResponseDto.ActivityInfo activityInfo =
    //            HomeResponseDto.ActivityInfo.builder().weeklyMissionStatus(weeklyStatus).build();

    // 5. 최종 DTO 빌드
    return HomeResponseDto.builder()
        .userInfo(userInfo)
        .gardenSummaries(gardenSummaries) // 변경된 부분
        .todayMissions(todayMissions)
        .build();
  }

  private long calculateRequiredExpForLevel(int level) {
    return level * 100L; // 예시 로직
  }

  private List<Boolean> createWeeklyMissionStatus(Long userId) {
    // 기존 로직과 동일
    LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
    List<UserDailyMission> recentMissions =
        userDailyMissionRepository.findByUserIdAndCreatedAtAfter(userId, sevenDaysAgo);

    Set<LocalDate> completedDates =
        recentMissions.stream()
            .filter(UserDailyMission::isCompleted)
            .map(mission -> mission.getCreatedAt().toLocalDate())
            .collect(Collectors.toSet());

    return Stream.iterate(LocalDate.now().minusDays(6), date -> date.plusDays(1))
        .limit(7)
        .map(completedDates::contains)
        .collect(Collectors.toList());
  }

  public PannelResponseDTO getPannelData(User user) {
    boolean isDiaryCompleted = false;
    boolean isQuizCompleted = false;
    boolean isCheckingCompleted = false;
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

    Diary diary =
        diaryRepository.findTodayDiaryByUser(user, startOfDay, endOfDay).stream()
            .findFirst()
            .orElse(null);

    if (diary != null && diary.getCreatedAt().isAfter(LocalDate.now().atStartOfDay())) {
      isDiaryCompleted = true;
    }
    UserQuiz userQuiz =
        userQuizRepository.findAllTodayUserQuizByUser(user, startOfDay, endOfDay).get(0);
    if (userQuiz != null && userQuiz.getIsCompleted()) { // 오늘의 퀴즈 성공시
      isQuizCompleted = true;
    }

    // 위시 트리 임시
    PannelResponseDTO.WishTreeDto wishTreeDto =
        PannelResponseDTO.WishTreeDto.builder()
            .currentStage("꽃") // 예시 데이터
            .currentPoints(1200) // 예시 데이터
            .requiredPointsForNextStage(1300) // 예시 데이터
            .build();

    return PannelResponseDTO.builder()
        .isDairyCompleted(isDiaryCompleted)
        .isQuizCompleted(isQuizCompleted)
        .isCheckingCompleted(isCheckingCompleted)
        .wishTree(wishTreeDto)
        .build();
  }
}
