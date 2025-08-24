package com.example.cp_main_be.domain.garden.garden.service;

import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.garden.domain.GardenBackground;
import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenBackgroundRepository;
import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenRepository;
import com.example.cp_main_be.domain.garden.garden.dto.GardenResponse;
import com.example.cp_main_be.domain.garden.garden.dto.response.GardenBackgroundCandidateResponse;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GardenService {

  private static final int MAX_GARDEN_COUNT = 4;
  private static final int WATERING_POINTS = 2;
  private static final int SUNLIGHT_POINTS = 3;

  private final GardenRepository gardenRepository;
  private final UserService userService;
  private final ApplicationEventPublisher eventPublisher;
  private final GardenBackgroundRepository gardenBackgroundRepository;
  private final UserRepository userRepository;

  public GardenResponse findGardenById(Long gardenId) {
    Garden garden =
        gardenRepository
            .findById(gardenId)
            .orElseThrow(() -> new IllegalArgumentException("Garden not found"));

    return GardenResponse.from(garden);
  }

  @Transactional
  public void waterGarden(Long actorId, Long gardenId) {
    Garden garden =
        gardenRepository
            .findById(gardenId)
            .orElseThrow(() -> new IllegalArgumentException("해당 텃밭을 찾을 수 없습니다."));

    User owner = garden.getUser(); // 정원 주인

    // Case 1: 자신의 정원에 물을 주는 경우
    if (owner.getId().equals(actorId)) {
      // 8시간 쿨타임 체크
      if (garden.getLastWateredByOwnerAt() != null
          && garden.getLastWateredByOwnerAt().plusHours(8).isAfter(LocalDateTime.now())) {
        throw new IllegalStateException("아직 물을 줄 수 없습니다. 8시간이 지나야 가능합니다.");
      }

      garden.increaseWaterCount();
      userService.addExperience(actorId, WATERING_POINTS);
      garden.recordOwnerWateringTime(); // 주인이 물 준 시간 기록
    }
    // Case 2: 남의 정원에 물을 주는 경우
    else {
      // TODO: 친구가 물을 주는 경우에도 쿨타임을 적용할지 정책 결정이 필요합니다.
      if (garden.getLastWateredByFriendAt() != null
          && garden.getLastWateredByFriendAt().plusHours(12).isAfter(LocalDateTime.now())) {
        throw new IllegalStateException("친구의 정원에는 12시간에 한 번만 물을 줄 수 있습니다.");
      }

      // 남한테 주는 경우에는 준 사람이 물 경험치를 받고 정원의 waterCount가 증가한다.
      userService.addExperience(actorId, WATERING_POINTS);
      garden.increaseWaterCount();
      garden.recordFriendWateringTime(); // 친구가 물 준 시간 기록
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

    garden.increaseSunlightCount();
    userService.addExperience(actorId, SUNLIGHT_POINTS);
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
      return;
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
}
