package com.example.cp_main_be.domain.garden.garden.service;

import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.garden.domain.GardenBackground;
import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenBackgroundRepository;
import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenRepository;
import com.example.cp_main_be.domain.garden.garden.dto.GardenResponse;
import com.example.cp_main_be.domain.garden.garden.dto.response.GardenBackgroundCandidateResponse;
import com.example.cp_main_be.domain.garden.garden.dto.response.GardenBackgroundResponse;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.global.event.WateredByFriendEvent;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    User owner = garden.getUser();

    // Case 1: 자신의 정원에 물을 주는 경우
    if (owner.getId().equals(actorId)) {
      garden.increaseWaterCount();
      userService.addExperience(actorId, WATERING_POINTS);
    }
    // Case 2: 다른 사람의 정원에 물을 주는 경우
    else {
      User actor = userService.findUserById(actorId);
      garden.increaseWaterCount();
      userService.addExperience(actorId, WATERING_POINTS); // 물을 준 사람에게 포인트 지급
      eventPublisher.publishEvent(new WateredByFriendEvent(owner, actor)); // 정원 주인에게 알림
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
  public void unlockGarden(User user) {
    int currentGardens = user.getGardens().size();
    long userLevel = user.getLevel();

    // 최대 텃밭 개수(4개)를 초과하는지 확인
    if (currentGardens >= MAX_GARDEN_COUNT) {
      throw new IllegalStateException("텃밭은 최대 " + MAX_GARDEN_COUNT + "개까지만 생성할 수 있습니다.");
    }

    // 사용자의 레벨이 현재 보유한 텃밭 수보다 많아야 새 텃밭을 열 수 있음
    if (userLevel <= currentGardens) {
      throw new IllegalStateException("레벨이 부족하여 더 이상 텃밭을 잠금 해제할 수 없습니다.");
    }

    // 새 텃밭 생성 (슬롯 번호는 기존 텃밭 수 + 1)
    Garden newGarden = Garden.builder().user(user).slotNumber(currentGardens + 1).build();

    gardenRepository.save(newGarden);

    // User 엔티티의 gardens 리스트에도 추가하여 영속성 컨텍스트와 객체 상태의 일관성을 맞춤
    user.addGarden(newGarden);
  }

  public GardenBackgroundResponse getGardenBackgroundImage(Long gardenId) {
    Garden garden =
        gardenRepository
            .findById(gardenId)
            .orElseThrow(() -> new IllegalArgumentException("해당 텃밭을 찾을 수 없습니다."));

    GardenBackground background = garden.getGardenBackground();
    if (background == null) {
      throw new IllegalStateException("해당 텃밭에 배경화면이 설정되어 있지 않습니다.");
    }

    return new GardenBackgroundResponse(background.getImageUrl());
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
