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
import com.example.cp_main_be.domain.mission.wishTree.WishTreeService;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.event.WishTreeEvolvedEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GardenService {

  private static final Long WATERING_POINTS = 2L;
  private static final Long SUNLIGHT_POINTS = 3L;
  private static final Long MAX_FRIEND_WATERING_PER_DAY = 3L;
  private final WishTreeService wishTreeService;
  private final GardenRepository gardenRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final GardenBackgroundRepository gardenBackgroundRepository;
  private final UserRepository userRepository;
  private final FriendWateringLogRepository friendWateringLogRepository;
  private final com.example.cp_main_be.domain.member.log.domain.repository
          .UserDailyActivityLogRepository
      userDailyActivityLogRepository;

  public GardenResponse findGardenById(Long gardenId) {
    Garden garden =
        gardenRepository
            .findById(gardenId)
            .orElseThrow(() -> new IllegalArgumentException("Garden not found"));

    garden.recordAccess();
    // [추가] 마지막 방문 정원 ID 기록
    garden.getUser().updateLastVisitedGarden(garden.getId());

    return GardenResponse.from(garden);
  }

  @Transactional
  public void waterGarden(Long actorId, Long gardenId) {
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
  @Transactional
  public void waterOwnGarden(Long ownerId, Garden garden) {
    // [수정] Garden 객체에게 직접 물을 줄 수 있는지 물어봅니다.
    if (!garden.isWaterableByOwner()) {
      throw new CustomApiException(ErrorCode.WATERING_COOL_DOWN);
    }

    User owner = garden.getUser();
    garden.increaseWaterCount();
    wishTreeService.addPointsToWishTree(ownerId, WATERING_POINTS);
    garden.recordOwnerWateringTime(); // 주인이 물 준 시간 기록
    garden.recordAccess();

    // [추가] 오늘 물주기 활동 기록
    recordDailyActivity(owner, true, false);
  }

  /** 친구의 정원에 물을 주는 로직을 처리합니다. */
  @Transactional
  public void waterFriendGarden(Long actorId, Garden garden) {
    User actor =
        userRepository
            .findById(actorId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

    LocalDateTime startOfToday = getStartOfToday();

    checkFriendWateringLimit(actor, startOfToday);
    checkAlreadyWateredToday(actor, garden, startOfToday);

    wishTreeService.addPointsToWishTree(actor.getId(), WATERING_POINTS);
    garden.increaseWaterCount();
    garden.recordFriendWateringTime();

    LocalDateTime nowInSeoul = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

    FriendWateringLog log =
        FriendWateringLog.builder()
            .waterGiver(actor)
            .wateredGarden(garden)
            .wateredAt(nowInSeoul)
            .build();
    friendWateringLogRepository.save(log);
  }

  private void checkFriendWateringLimit(User actor, LocalDateTime startOfWateringDay) {
    long todayWateringCount =
        friendWateringLogRepository.countByWaterGiverAndWateredAtAfter(actor, startOfWateringDay);
    if (todayWateringCount >= MAX_FRIEND_WATERING_PER_DAY) {
      throw new CustomApiException(ErrorCode.FRIEND_WATERING_LIMIT_EXCEEDED);
    }
  }

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
            .orElseThrow(() -> new CustomApiException(ErrorCode.GARDEN_NOT_FOUND));

    if (!garden.getUser().getId().equals(actorId)) {
      throw new CustomApiException(ErrorCode.ACCESS_DENIED, "자신의 정원에만 햇빛을 줄 수 있습니다.");
    }

    LocalDateTime startOfSunlightDay = getStartOfCurrentSunlightDay();
    if (garden.getLastSunlightReceivedAt() != null
        && garden.getLastSunlightReceivedAt().isAfter(startOfSunlightDay)) {
      throw new CustomApiException(
          ErrorCode.SUNLIGHT_COOL_DOWN, "이미 햇빛을 주었습니다. 내일 오전 6시 이후에 다시 시도해주세요.");
    }

    garden.increaseSunlightCount();
    wishTreeService.addPointsToWishTree(actorId, SUNLIGHT_POINTS);
    garden.recordSunlightTime();
    garden.recordAccess();

    // [추가] 오늘 햇빛주기 활동 기록
    User user =
        userRepository
            .findById(actorId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
    recordDailyActivity(user, false, true);
  }

  @Transactional
  public void unlockNextGarden(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));

    if (user.getUnlockableGardenCount() <= 0) {
      throw new CustomApiException(ErrorCode.GARDEN_SLOT_LOCKED, "해금할 수 있는 정원이 없습니다.");
    }

    // 먼저 정원 조회 (실패 시 여기서 예외)
    Garden gardenToUnlock =
        gardenRepository
            .findFirstByUserAndIsLockedIsTrueOrderBySlotNumberAsc(user)
            .orElseThrow(() -> new CustomApiException(ErrorCode.GARDEN_NOT_FOUND, "해금할 정원이 없습니다."));

    // 정원이 존재할 때만 실행
    gardenToUnlock.unlock();
    user.decrementUnlockableGardenCount(); // 성공 확정 후 카운트 감소
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

  /** 친구에게 물 주는 횟수가 초기화되는 시간(자정)을 반환합니다. */
  private LocalDateTime getStartOfToday() {
    return LocalDate.now(ZoneId.of("Asia/Seoul")).atStartOfDay();
  }

  private LocalDateTime getStartOfCurrentSunlightDay() {
    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    LocalDateTime todaySixAM = now.toLocalDate().atTime(6, 0);

    if (now.isBefore(todaySixAM)) {
      return todaySixAM.minusDays(1);
    } else {
      return todaySixAM;
    }
  }

  @Scheduled(cron = "0 0 4 * * *")
  @Transactional
  public void cleanupOldWateringLogs() {
    final int RETENTION_DAYS = 7;
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);
    log.info("Starting cleanup of friend watering logs older than {} days...", RETENTION_DAYS);
    int deletedCount = friendWateringLogRepository.deleteByWateredAtBefore(cutoffDate);
    log.info("Finished cleanup. Deleted {} old friend watering logs.", deletedCount);
  }

  private void recordDailyActivity(User user, boolean watered, boolean sunlight) {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    com.example.cp_main_be.domain.member.log.domain.UserDailyActivityLog log =
        userDailyActivityLogRepository
            .findByUserAndDate(user, today)
            .orElseGet(
                () ->
                    com.example.cp_main_be.domain.member.log.domain.UserDailyActivityLog.builder()
                        .user(user)
                        .date(today)
                        .hasWatered(false)
                        .hasSunlight(false)
                        .build());

    if (watered) log.updateWatered(true);
    if (sunlight) log.updateSunlight(true);

    userDailyActivityLogRepository.save(log);
  }

  @EventListener
  @Transactional
  public void handleWishTreeEvolved(WishTreeEvolvedEvent event) {
    unlockNextGarden(event.getUserId());
  }
}
