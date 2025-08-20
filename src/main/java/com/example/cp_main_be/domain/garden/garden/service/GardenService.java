package com.example.cp_main_be.domain.garden.garden.service;

import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenRepository;
import com.example.cp_main_be.domain.garden.garden.dto.GardenResponse;
import com.example.cp_main_be.domain.member.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GardenService {

  private static final int MAX_GARDEN_COUNT = 3;

  private final GardenRepository gardenRepository;

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
    long userLevel = user.getLevel();

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
}
