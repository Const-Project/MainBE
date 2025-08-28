package com.example.cp_main_be.domain.home.service;

import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.home.HomeResponseDto;
import com.example.cp_main_be.domain.home.PannelResponseDTO;
import com.example.cp_main_be.domain.member.daily_question.domain.repository.DailyQuestionAnswerRepository;
import com.example.cp_main_be.domain.member.notification.domain.repository.NotificationRepository;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.mission.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserDailyMission;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.repository.UserDailyMissionRepository;
import com.example.cp_main_be.domain.mission.wishTree.WishTree;
import com.example.cp_main_be.domain.mission.wishTree.WishTreeRepository;
import com.example.cp_main_be.domain.mission.wishTree.WishTreeService;
import com.example.cp_main_be.domain.mission.wishTree.WishTreeStage;
import com.example.cp_main_be.domain.realquiz.repository.UserQuizRepository;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
  private final UserDailyMissionRepository userDailyMissionRepository;
  private final NotificationRepository notificationRepository;
  private final DiaryRepository diaryRepository;
  private final UserQuizRepository userQuizRepository;
  private final WishTreeService wishTreeService;
  private final DailyQuestionAnswerRepository dailyQuestionAnswerRepository; // <-- 추가
  private final WishTreeRepository wishTreeRepository;

  // ...

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

    WishTree wishTreeOfUser = user.getWishTree();
    // 1. UserInfo 구성
    int unreadNotificationCount = notificationRepository.countByReceiverAndIsReadFalse(user);
    HomeResponseDto.UserInfo userInfo =
        HomeResponseDto.UserInfo.builder()
            .id(user.getId())
            .username(user.getNickname())
            .level(wishTreeOfUser.getStage().ordinal() + 1)
            .currentExp(wishTreeOfUser.getPoints())
            .requiredExpForNextLevel(wishTreeOfUser.getStage().getRequiredPointsForNextStage())
            .unreadNotificationCount(unreadNotificationCount)
            .build();

    WishTree wishTree =
        wishTreeRepository
            .findByUserId(userId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));

    long unlockedGardenCount = user.getGardens().stream().filter(g -> !g.isLocked()).count();

    Map<Integer, Garden> userGardens =
        user.getGardens().stream()
            .collect(Collectors.toMap(Garden::getSlotNumber, garden -> garden));
    // 2. GardenSummaries 구성 (모든 정원 순회)
    List<HomeResponseDto.GardenSummaryInfo> gardenSummaries =
        IntStream.rangeClosed(1, 4)
            .mapToObj(
                slotNumber -> {
                  Garden garden = userGardens.get(slotNumber);

                  if (garden != null && !garden.isLocked()) {
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
                        .build();
                  } else {
                    boolean isUnlockable =
                        garden != null
                            && wishTree.isUnlockable()
                            && garden.getSlotNumber() == unlockedGardenCount + 1;

                    return HomeResponseDto.GardenSummaryInfo.builder()
                        .gardenId(garden != null ? garden.getId() : null)
                        .gardenSlotNumber(slotNumber)
                        .avatar(null)
                        .isLocked(true)
                        .isUnlockable(isUnlockable)
                        .isOwnerWateringAble(false)
                        .isOwnerSunlightAble(false)
                        .build();
                  }
                })
            .collect(Collectors.toList());

    // 3. TodayMissions 구성 (수정된 로직)
    LocalDate today = LocalDate.now();
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(23, 59, 59);

    // 미션 1: 일기 쓰기 완료 여부 확인
    boolean isDiaryCompleted =
        diaryRepository.existsByUserAndCreatedAtBetween(user, startOfDay, endOfDay);

    // 미션 2: 퀴즈 풀기 완료 여부 확인
    boolean isQuizCompleted =
        userQuizRepository.existsByUserAndIsCompletedIsTrueAndCreatedAtBetween(
            user, startOfDay, endOfDay);

    // 미션 3: 오늘의 질문 답변 완료 여부 확인
    boolean isCheckingCompleted =
        dailyQuestionAnswerRepository.existsByUserAndAnsweredDate(user, today);

    // 각 미션의 완료 상태를 바탕으로 MissionInfo DTO 리스트 생성
    List<HomeResponseDto.MissionInfo> todayMissions =
        List.of(
            HomeResponseDto.MissionInfo.builder()
                .missionId(1L) // 임의의 ID 부여
                .missionTitle("오늘의 일기 쓰기")
                .missionType("DIARY")
                .isCompleted(isDiaryCompleted)
                .build(),
            HomeResponseDto.MissionInfo.builder()
                .missionId(2L) // 임의의 ID 부여
                .missionTitle("오늘의 퀴즈 풀기")
                .missionType("QUIZ")
                .isCompleted(isQuizCompleted)
                .build(),
            HomeResponseDto.MissionInfo.builder()
                .missionId(3L) // 임의의 ID 부여
                .missionTitle("오늘의 질문 답변하기")
                .missionType("CHECKING") // 또는 적절한 타입
                .isCompleted(isCheckingCompleted)
                .build());

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
    LocalDate today = LocalDate.now();
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

    // exists 쿼리로 간단하게 변경
    boolean isDiaryCompleted =
        diaryRepository.existsByUserAndCreatedAtBetween(user, startOfDay, endOfDay);
    boolean isQuizCompleted =
        userQuizRepository.existsByUserAndIsCompletedIsTrueAndCreatedAtBetween(
            user, startOfDay, endOfDay);
    // 오늘의 질문 완료 여부 확인 로직 추가
    boolean isCheckingCompleted =
        dailyQuestionAnswerRepository.existsByUserAndAnsweredDate(user, today);

    // 위시트리 찾기
    WishTree wishTree = wishTreeService.findOrCreateWishTree(user.getId());

    WishTreeStage currentStageEnum = wishTree.getStage();
    WishTreeStage nextStageEnum = currentStageEnum.getNextStage();

    PannelResponseDTO.WishTreeDto wishTreeDto =
        PannelResponseDTO.WishTreeDto.builder()
            .currentStage(currentStageEnum.getKoreanName())
            // nextStage가 null이 아니면 이름을, null이면 빈 문자열("")을 설정
            .nextStage(nextStageEnum != null ? nextStageEnum.getKoreanName() : "")
            .currentPoints(wishTree.getPoints())
            .requiredPointsForNextStage(
                currentStageEnum.getRequiredPointsForNextStage() - wishTree.getPoints())
            .progressPercent(
                (long)
                    ((double) wishTree.getPoints()
                        / currentStageEnum.getRequiredPointsForNextStage()
                        * 100))
            .build();

    return PannelResponseDTO.builder()
        .isDairyCompleted(isDiaryCompleted) // 변수명 오타 수정: isDiaryCompleted
        .isQuizCompleted(isQuizCompleted)
        .isCheckingCompleted(isCheckingCompleted) // 이제 정상적으로 값이 들어감
        .wishTree(wishTreeDto)
        .build();
  }
}
