package com.example.cp_main_be.domain.garden.garden.service;

import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.garden.domain.GardenBackground;
import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenBackgroundRepository;
import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenRepository;
import com.example.cp_main_be.domain.garden.garden.dto.response.GardenBackgroundCandidateResponse;
import com.example.cp_main_be.domain.garden.garden.dto.response.GardenResponse;
import com.example.cp_main_be.domain.garden.wateringlog.domain.FriendWateringLog;
import com.example.cp_main_be.domain.garden.wateringlog.domain.repository.FriendWateringLogRepository;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GardenService {

  private static final int MAX_GARDEN_COUNT = 3;
  private static final int WATERING_POINTS = 2;
  private static final int SUNLIGHT_POINTS = 3;
  private static final int MAX_FRIEND_WATERING_PER_DAY = 3;

  private final GardenRepository gardenRepository;
  private final UserService userService;
  private final ApplicationEventPublisher eventPublisher;
  private final GardenBackgroundRepository gardenBackgroundRepository;
  private final UserRepository userRepository;
  private final FriendWateringLogRepository friendWateringLogRepository;

  public GardenResponse findGardenById(Long gardenId) {
    Garden garden =
        gardenRepository
            .findById(gardenId)
            .orElseThrow(() -> new IllegalArgumentException("Garden not found"));

    return GardenResponse.from(garden);
  }

  @Transactional
  public void waterGarden(Long actorId, Long gardenId) {
    // N+1 문제를 방지하기 위해 Garden과 User를 함께 조회하는 것을 권장합니다.
    // 예: gardenRepository.findByIdWithUser(gardenId)
    Garden garden =
        gardenRepository
            .findById(gardenId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.GARDEN_NOT_FOUND));

    User owner = garden.getUser();

    if (owner.getId().equals(actorId)) {
      waterOwnGarden(actorId, garden);
    } else {
      waterFriendGarden(actorId, garden);
    }
  }

  /** 자신의 정원에 물을 주는 로직을 처리합니다. */
  private void waterOwnGarden(Long ownerId, Garden garden) {
    // 8시간 쿨타임 체크
    if (garden.getLastWateredByOwnerAt() != null
        && garden.getLastWateredByOwnerAt().plusHours(8).isAfter(LocalDateTime.now())) {
      throw new CustomApiException(ErrorCode.WATERING_COOL_DOWN);
    }

    garden.increaseWaterCount();
    userService.addExperience(ownerId, WATERING_POINTS);
    garden.recordOwnerWateringTime(); // 주인이 물 준 시간 기록
  }

  /** 친구의 정원에 물을 주는 로직을 처리합니다. */
  private void waterFriendGarden(Long actorId, Garden garden) {
    User actor =
        userRepository
            .findById(actorId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

    // 낮 12시 이후 -> 그날의 12시 날짜 반환, 이전 -> 전날의 12시 날짜 반환
    LocalDateTime startOfWateringDay = getStartOfCurrentWateringDay();

    // 1. 하루에 3회 제한 체크
    checkFriendWateringLimit(actor, startOfWateringDay);

    // 2. 같은 정원에 하루 한 번 제한 체크
    checkAlreadyWateredToday(actor, garden, startOfWateringDay);

    // 남한테 주는 경우에는 준 사람이 물 경험치를 받고 정원의 waterCount가 증가한다.
    userService.addExperience(actorId, WATERING_POINTS);
    garden.increaseWaterCount();

    // 물주기 활동 기록
    FriendWateringLog log =
        FriendWateringLog.builder()
            .waterGiver(actor)
            .wateredGarden(garden) // wateredAt은 @CreatedDate가 자동으로 설정합니다.
            .build();
    friendWateringLogRepository.save(log);
  }

  // 물주기 남은 횟수 확인
  private void checkFriendWateringLimit(User actor, LocalDateTime startOfWateringDay) {
    int todayWateringCount =
        friendWateringLogRepository.countByWaterGiverAndWateredAtAfter(actor, startOfWateringDay);
    if (todayWateringCount >= MAX_FRIEND_WATERING_PER_DAY) {
      throw new CustomApiException(ErrorCode.FRIEND_WATERING_LIMIT_EXCEEDED);
    }
  }

  // 당일에 해당 정원에 이미 물을 주었는지 확인
  private void checkAlreadyWateredToday(
      User actor, Garden garden, LocalDateTime startOfWateringDay) {
    boolean alreadyWatered =
        friendWateringLogRepository.existsByWaterGiverAndWateredGardenAndWateredAtAfter(
            actor, garden, startOfWateringDay);
    if (alreadyWatered) {
      throw new CustomApiException(ErrorCode.ALREADY_WATERED_GARDEN);
    }
  }

  @Transactional
  public void sunlightGarden(Long actorId, Long gardenId) {
    Garden garden =
        gardenRepository
            .findById(gardenId)
            .orElseThrow(() -> new IllegalArgumentException("해당 텃밭을 찾을 수 없습니다."));

    // 햇빛은 본인만 줄 수 있도록 검증
    if (!garden.getUser().getId().equals(actorId)) {
      throw new IllegalStateException("자신의 정원에만 햇빛을 줄 수 있습니다.");
    }

    // 하루에 한 번만 햇빛을 줄 수 있도록 체크 (초기화 시간: 오전 6시)
    LocalDateTime startOfSunlightDay = getStartOfCurrentSunlightDay();
    if (garden.getLastSunlightReceivedAt() != null
        && garden.getLastSunlightReceivedAt().isAfter(startOfSunlightDay)) {
      throw new IllegalStateException("오늘은 이미 햇빛을 주었습니다. 내일 오전 6시 이후에 다시 시도해주세요.");
    }

    garden.increaseSunlightCount();
    userService.addExperience(actorId, SUNLIGHT_POINTS);
    garden.recordSunlightTime(); // 햇빛 준 시간 기록
  }

  @Transactional
  public void unlockNewGardenSlot(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));

    int currentGardens = user.getGardens().size();

    // 최대 텃밭 개수 제한은 여전히 유효하므로 여기서 검사
    if (currentGardens >= MAX_GARDEN_COUNT) {
      // 이미 최대치이므로 조용히 종료하거나 예외를 던질 수 있습니다.
      // 여기서는 추가 생성을 막고 그냥 리턴합니다.
      throw new CustomApiException(ErrorCode.GARDEN_SLOT_MAXED_OUT);
    }

    // [기존 레벨 체크 로직 삭제!]

    // TODO: 새로 생성된 텃밭의 기본 Avatar, Background 설정 로직 필요
    Garden newGarden = Garden.builder().user(user).slotNumber(currentGardens + 1).build();

    gardenRepository.save(newGarden);

    // User 엔티티의 gardens 리스트에도 추가
    user.addGarden(newGarden);
  }

  @Transactional
  public void updateGardenBackgroundImage(Long gardenId, Long backgroundId) {
    Garden garden =
        gardenRepository
            .findById(gardenId)
            .orElseThrow(() -> new IllegalArgumentException("해당 텃밭을 찾을 수 없습니다."));

    GardenBackground newBackground =
        gardenBackgroundRepository
            .findById(backgroundId)
            .orElseThrow(() -> new IllegalArgumentException("해당 배경화면을 찾을 수 없습니다."));

    garden.updateBackgroundImage(newBackground);
  }

  public List<GardenBackgroundCandidateResponse> getAllBackgrounds() {
    return gardenBackgroundRepository.findAll().stream()
        .map(GardenBackgroundCandidateResponse::from)
        .collect(Collectors.toList());
  }

  /**
   * 현재 시간 기준으로 물주기 횟수가 초기화되는 시간(정오)을 계산합니다. - 현재 시간이 정오 이전이면, 어제 정오를 반환합니다. - 현재 시간이 정오 이후이면, 오늘
   * 정오를 반환합니다.
   *
   * @return 현재 물주기 주기의 시작 시간
   */
  private LocalDateTime getStartOfCurrentWateringDay() {
    // 서버 위치와 관계없이 항상 한국 시간 기준으로 동작하도록 시간대를 명시합니다.
    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    LocalDateTime todayNoon = now.toLocalDate().atTime(12, 0);

    if (now.isBefore(todayNoon)) {
      return todayNoon.minusDays(1);
    } else {
      return todayNoon;
    }
  }

  /**
   * 현재 시간 기준으로 햇빛 주기 횟수가 초기화되는 시간(오전 6시)을 계산합니다. - 현재 시간이 오전 6시 이전이면, 어제 오전 6시를 반환합니다. - 현재 시간이 오전
   * 6시 이후이면, 오늘 오전 6시를 반환합니다.
   *
   * @return 현재 햇빛 주기의 시작 시간
   */
  private LocalDateTime getStartOfCurrentSunlightDay() {
    // 서버 위치와 관계없이 항상 한국 시간 기준으로 동작하도록 시간대를 명시합니다.
    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    LocalDateTime todaySixAM = now.toLocalDate().atTime(6, 0);

    if (now.isBefore(todaySixAM)) {
      return todaySixAM.minusDays(1);
    } else {
      return todaySixAM;
    }
  }

  /** 오래된 친구 물주기 로그를 주기적으로 삭제하는 스케줄링 작업입니다. cron = "0 0 4 * * *" : 매일 새벽 4시에 실행됩니다. */
  @Scheduled(cron = "0 0 4 * * *")
  @Transactional // 쓰기 작업이므로 클래스 레벨의 readOnly 설정을 오버라이드합니다.
  public void cleanupOldWateringLogs() {
    // 7일 이상된 기록을 삭제하도록 설정. 이 값은 application.yml에서 관리하는 것이 더 좋습니다.
    final int RETENTION_DAYS = 7;
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);
    log.info("Starting cleanup of friend watering logs older than {} days...", RETENTION_DAYS);
    int deletedCount = friendWateringLogRepository.deleteByWateredAtBefore(cutoffDate);
    log.info("Finished cleanup. Deleted {} old friend watering logs.", deletedCount);
  }
}
