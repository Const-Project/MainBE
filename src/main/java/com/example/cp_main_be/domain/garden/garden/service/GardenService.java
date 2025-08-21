package com.example.cp_main_be.domain.garden.garden.service;

import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.garden.domain.GardenBackground;
import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenBackgroundRepository;
import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenRepository;
import com.example.cp_main_be.domain.garden.garden.dto.response.GardenBackgroundCandidateResponse;
import com.example.cp_main_be.domain.garden.garden.dto.response.GardenBackgroundResponse;
import com.example.cp_main_be.domain.garden.garden.dto.response.GardenResponse;
import com.example.cp_main_be.domain.member.user.domain.User;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GardenService {

  private static final int MAX_GARDEN_COUNT = 3;

  private final GardenRepository gardenRepository;
  private final GardenBackgroundRepository gardenBackgroundRepository;

  public GardenResponse findGardenById(Long gardenId) {
    Garden garden =
        gardenRepository
            .findById(gardenId)
            .orElseThrow(() -> new IllegalArgumentException("Garden not found"));

    return GardenResponse.from(garden);
  }

  @Transactional
  public void waterGarden(Long gardenId) {
    Garden garden =
        gardenRepository
            .findById(gardenId)
            .orElseThrow(() -> new IllegalArgumentException("해당 텃밭을 찾을 수 없습니다."));

    garden.increaseWaterCount();
  }

  @Transactional
  public void sunlightGarden(Long gardenId) {
    Garden garden =
        gardenRepository
            .findById(gardenId)
            .orElseThrow(() -> new IllegalArgumentException("해당 텃밭을 찾을 수 없습니다."));

    garden.increaseSunlightCount();
  }

  @Transactional
  public void unlockGarden(User user) {
    int currentGardens = user.getGardens().size();
    int userLevel = user.getLevel();

    // 최대 텃밭 개수(3개)를 초과하는지 확인
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
