package com.example.cp_main_be.domain.home.service;

import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.home.HomeResponseDto;
import com.example.cp_main_be.domain.home.PannelResponseDTO;
import com.example.cp_main_be.domain.member.daily_question.domain.repository.DailyQuestionAnswerRepository;
import com.example.cp_main_be.domain.member.notification.domain.repository.NotificationRepository;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.mission.daily_mission_master.MissionType;
import com.example.cp_main_be.domain.mission.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserDailyMission;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.repository.UserDailyMissionRepository;
import com.example.cp_main_be.domain.mission.wishTree.WishTree;
import com.example.cp_main_be.domain.mission.wishTree.WishTreeStage;
import com.example.cp_main_be.domain.realquiz.repository.UserQuizRepository;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

  private final UserRepository userRepository;
  private final NotificationRepository notificationRepository;
  private final DiaryRepository diaryRepository;
  private final DailyQuestionAnswerRepository dailyQuestionAnswerRepository;
  private final UserDailyMissionRepository userDailyMissionRepository;
  private final UserQuizRepository userQuizRepository;
  private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

  private LocalDateTime getStartOfCurrentSunlightDay() {
    LocalDateTime now = LocalDateTime.now(KOREA_ZONE);
    LocalDateTime todaySixAM = now.toLocalDate().atTime(6, 0);
    return now.isBefore(todaySixAM) ? todaySixAM.minusDays(1) : todaySixAM;
  }

  public HomeResponseDto getHomeScreenData(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));

    WishTree wishTree = user.getWishTree();
    if (wishTree == null) {
      throw new CustomApiException(ErrorCode.NOT_FOUND, "소망나무 정보를 찾을 수 없습니다.");
    }

    WishTreeStage currentStage = wishTree.getStage();
    long totalPoints = wishTree.getPoints();
    long expForCurrentLevelStart = currentStage.getRequiredPoints();
    long expForNextLevelStart = currentStage.getRequiredPointsForNextStage();
    long expInCurrentLevel = totalPoints - expForCurrentLevelStart;
    long expNeededForLevelUp = expForNextLevelStart - expForCurrentLevelStart;

    int unreadNotificationCount = notificationRepository.countByReceiverAndIsReadFalse(user);

    Integer lastAccessedSlotNumber = 1;

    if (user.getLastVisitedGardenId() != null) {
      lastAccessedSlotNumber =
          user.getGardens().stream()
              .filter(g -> g.getId().equals(user.getLastVisitedGardenId()))
              .findFirst()
              .map(Garden::getSlotNumber)
              .orElse(1);
    } else {
      lastAccessedSlotNumber =
          user.getGardens().stream()
              .filter(g -> g.getLastAccessedAt() != null)
              .max(java.util.Comparator.comparing(Garden::getLastAccessedAt))
              .map(Garden::getSlotNumber)
              .orElse(1);
    }

    HomeResponseDto.UserInfo userInfo =
        HomeResponseDto.UserInfo.builder()
            .id(user.getId())
            .username(user.getNickname())
            .level(currentStage.getLevel())
            .currentExp(expInCurrentLevel)
            .requiredExpForNextLevel(expNeededForLevelUp)
            .unreadNotificationCount(unreadNotificationCount)
            .lastAccessedSlotNumber(lastAccessedSlotNumber)
            .build();

    Map<Integer, Garden> userGardens =
        user.getGardens().stream()
            .collect(Collectors.toMap(Garden::getSlotNumber, garden -> garden));

    long unlockedGardenCount = user.getGardens().stream().filter(g -> !g.isLocked()).count();

    List<HomeResponseDto.GardenSummaryInfo> gardenSummaries =
        IntStream.rangeClosed(1, 4)
            .mapToObj(
                slotNumber -> {
                  Garden garden = userGardens.get(slotNumber);
                  if (garden != null && !garden.isLocked()) {
                    // [수정] Garden 객체에 직접 물어보는 방식으로 변경
                    boolean isOwnerWateringAble = garden.isWaterableByOwner();
                    long waterableInSeconds = garden.getWaterableByOwnerInSeconds();
                    LocalDateTime nextWaterableAt =
                        isOwnerWateringAble ? null : garden.getLastWateredByOwnerAt().plusHours(4);

                    boolean isOwnerSunlightAble =
                        garden.getLastSunlightReceivedAt() == null
                            || garden
                                .getLastSunlightReceivedAt()
                                .isBefore(getStartOfCurrentSunlightDay());

                    HomeResponseDto.AvatarInfo avatarInfo = null;
                    if (garden.getAvatar() != null) {
                      avatarInfo =
                          HomeResponseDto.AvatarInfo.builder()
                              .avatarId(garden.getAvatar().getId())
                              .avatarName(garden.getAvatar().getNickname())
                              .avatarImageUrl(garden.getAvatar().getImageUrl())
                              .build();
                    }

                    return HomeResponseDto.GardenSummaryInfo.builder()
                        .gardenId(garden.getId())
                        .gardenSlotNumber(slotNumber)
                        .avatar(avatarInfo)
                        .isLocked(false)
                        .isUnlockable(false)
                        .isOwnerWateringAble(isOwnerWateringAble)
                        .isOwnerSunlightAble(isOwnerSunlightAble)
                        .waterableInSeconds(waterableInSeconds)
                        .nextWaterableAt(nextWaterableAt)
                        .build();
                  } else {
                    boolean isUnlockable =
                        garden != null
                            && garden.isLocked()
                            && slotNumber > unlockedGardenCount
                            && slotNumber <= unlockedGardenCount + user.getUnlockableGardenCount();

                    return HomeResponseDto.GardenSummaryInfo.builder()
                        .gardenId(garden != null ? garden.getId() : null)
                        .gardenSlotNumber(slotNumber)
                        .isLocked(true)
                        .isUnlockable(isUnlockable)
                        .build();
                  }
                })
            .collect(Collectors.toList());

    LocalDate today = LocalDate.now(KOREA_ZONE);
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(23, 59, 59);

    boolean isDiaryCompleted =
        diaryRepository.existsByUserAndCreatedAtBetween(user, startOfDay, endOfDay);
    List<UserDailyMission> todayMissionEntities =
        userDailyMissionRepository.findTodayMissionsWithMasterByUser(user, startOfDay, endOfDay);

    boolean isQuizCompleted =
        todayMissionEntities.stream()
                .filter(
                    mission -> mission.getDailyMissionMaster().getMissionType() == MissionType.QUIZ)
                .anyMatch(UserDailyMission::isCompleted)
            || userQuizRepository.existsByUserAndIsCompletedIsTrueAndCreatedAtBetween(
                user, startOfDay, endOfDay);

    boolean isQuizResultAvailable =
        todayMissionEntities.stream()
            .filter(mission -> mission.getDailyMissionMaster().getMissionType() == MissionType.QUIZ)
            .anyMatch(
                mission ->
                    mission.getCompletedAt() != null
                        && mission.getCompletedAt().toLocalDate().equals(today)
                        && mission.getSelectedAnswerNumber() != null);
    boolean isCheckingCompleted =
        dailyQuestionAnswerRepository.existsByUserAndAnsweredDate(user, today);
    Long todayDiaryId =
        diaryRepository
            .findTopByUserAndCreatedAtBetweenOrderByCreatedAtDesc(user, startOfDay, endOfDay)
            .map(diary -> diary.getId())
            .orElse(null);

    List<HomeResponseDto.MissionInfo> todayMissions =
        List.of(
            HomeResponseDto.MissionInfo.builder()
                .missionId(1L)
                .missionTitle("오늘의 일기 쓰기")
                .missionType("DIARY")
                .isCompleted(isDiaryCompleted)
                .build(),
            HomeResponseDto.MissionInfo.builder()
                .missionId(2L)
                .missionTitle("오늘의 퀴즈 풀기")
                .missionType("QUIZ")
                .isCompleted(isQuizCompleted)
                .build(),
            HomeResponseDto.MissionInfo.builder()
                .missionId(3L)
                .missionTitle("오늘의 질문 답변하기")
                .missionType("CHECKING")
                .isCompleted(isCheckingCompleted)
                .build());

    return HomeResponseDto.builder()
        .userInfo(userInfo)
        .gardenSummaries(gardenSummaries)
        .todayMissions(todayMissions)
        .todayDiaryId(todayDiaryId)
        .build();
  }

  public PannelResponseDTO getPannelData(User user) {
    LocalDate today = LocalDate.now(KOREA_ZONE);
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(23, 59, 59);

    boolean isDiaryCompleted =
        diaryRepository.existsByUserAndCreatedAtBetween(user, startOfDay, endOfDay);
    List<UserDailyMission> todayMissionEntities =
        userDailyMissionRepository.findTodayMissionsWithMasterByUser(user, startOfDay, endOfDay);

    boolean isQuizCompleted =
        todayMissionEntities.stream()
                .filter(
                    mission -> mission.getDailyMissionMaster().getMissionType() == MissionType.QUIZ)
                .anyMatch(UserDailyMission::isCompleted)
            || userQuizRepository.existsByUserAndIsCompletedIsTrueAndCreatedAtBetween(
                user, startOfDay, endOfDay);

    boolean isQuizResultAvailable =
        todayMissionEntities.stream()
            .filter(mission -> mission.getDailyMissionMaster().getMissionType() == MissionType.QUIZ)
            .anyMatch(
                mission ->
                    mission.getCompletedAt() != null
                        && mission.getCompletedAt().toLocalDate().equals(today)
                        && mission.getSelectedAnswerNumber() != null);
    boolean isCheckingCompleted =
        dailyQuestionAnswerRepository.existsByUserAndAnsweredDate(user, today);

    WishTree wishTree = user.getWishTree();
    if (wishTree == null) {
      throw new CustomApiException(ErrorCode.NOT_FOUND, "소망나무 정보를 찾을 수 없습니다.");
    }
    WishTreeStage currentStageEnum = wishTree.getStage();
    WishTreeStage nextStageEnum = currentStageEnum.getNextStage();
    long totalPoints = wishTree.getPoints();

    long expForCurrentLevelStart = currentStageEnum.getRequiredPoints();
    long expForNextLevelStart = currentStageEnum.getRequiredPointsForNextStage();
    long expInCurrentLevel = totalPoints - expForCurrentLevelStart;
    long expNeededForLevelUp = expForNextLevelStart - expForCurrentLevelStart;

    long progressPercent =
        (expNeededForLevelUp > 0)
            ? (long) (((double) expInCurrentLevel / expNeededForLevelUp) * 100)
            : 100;

    PannelResponseDTO.WishTreeDto wishTreeDto =
        PannelResponseDTO.WishTreeDto.builder()
            .currentStage(currentStageEnum.getKoreanName())
            .nextStage(nextStageEnum != null ? nextStageEnum.getKoreanName() : "")
            .currentPoints(expInCurrentLevel)
            .requiredPointsForNextStage(expNeededForLevelUp)
            .progressPercent(progressPercent)
            .build();

    return PannelResponseDTO.builder()
        .isDairyCompleted(isDiaryCompleted)
        .isQuizCompleted(isQuizCompleted)
        .isCheckingCompleted(isCheckingCompleted)
        .isQuizResultAvailable(isQuizResultAvailable)
        .wishTree(wishTreeDto)
        .build();
  }
}
