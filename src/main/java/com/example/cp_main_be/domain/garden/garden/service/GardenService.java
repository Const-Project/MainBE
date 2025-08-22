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
