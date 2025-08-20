package com.example.cp_main_be.domain.garden.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenRepository;
import com.example.cp_main_be.domain.garden.garden.dto.GardenResponse;
import com.example.cp_main_be.domain.garden.garden.service.GardenService;
import com.example.cp_main_be.domain.member.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GardenServiceTest {

  @Mock private GardenRepository gardenRepository;

  @InjectMocks private GardenService gardenService;

  @DisplayName("텃밭 ID로 조회 성공")
  @Test
  void findGardenById_Success() {
    // given
    Long gardenId = 1L;
    User user = User.builder().id(1L).uuid(UUID.randomUUID()).username("testuser").build();
    Garden garden = Garden.builder().user(user).slotNumber(1).build();

    given(gardenRepository.findById(gardenId)).willReturn(Optional.of(garden));

    // when
    GardenResponse gardenResponse = gardenService.findGardenById(gardenId);

    // then
    assertThat(gardenResponse.getId()).isEqualTo(garden.getId());
    assertThat(gardenResponse.getUserId()).isEqualTo(user.getId());
    assertThat(gardenResponse.getSlotNumber()).isEqualTo(garden.getSlotNumber());
  }

  @DisplayName("텃밭 ID로 조회 실패 - 존재하지 않는 텃밭")
  @Test
  void findGardenById_NotFound() {
    // given
    Long gardenId = 999L;
    given(gardenRepository.findById(gardenId)).willReturn(Optional.empty());

    // when & then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          gardenService.findGardenById(gardenId);
        });
  }

  @DisplayName("텃밭 물주기 성공")
  @Test
  void waterGarden_Success() {
    // given
    Long gardenId = 1L;
    User user = User.builder().id(1L).uuid(UUID.randomUUID()).username("testuser").build();
    Garden garden = Garden.builder().user(user).slotNumber(1).build();

    given(gardenRepository.findById(gardenId)).willReturn(Optional.of(garden));

    // when
    gardenService.waterGarden(gardenId);

    // then
    assertThat(garden.getWaterCount()).isEqualTo(1);
  }

  @DisplayName("텃밭 햇빛 주기 성공")
  @Test
  void sunlightGarden_Success() {
    // given
    Long gardenId = 1L;
    User user = User.builder().id(1L).uuid(UUID.randomUUID()).username("testuser").build();
    Garden garden = Garden.builder().user(user).slotNumber(1).build();
    given(gardenRepository.findById(gardenId)).willReturn(Optional.of(garden));

    // when
    gardenService.sunlightGarden(gardenId);

    // then
    assertThat(garden.getSunlightCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("성공 - 레벨이 충분할 때 새로운 텃밭을 잠금 해제한다")
  void unlockGarden_Success() {
    // given
    // 레벨 2이고, 텃밭을 1개 가지고 있는 사용자
    User user =
        User.builder()
            .id(1L)
            .level(2)
            .gardens(new ArrayList<>(List.of(Garden.builder().slotNumber(1).build())))
            .build();

    // when
    gardenService.unlockGarden(user);

    // then
    // 1. gardenRepository.save()가 호출되었는지 검증
    ArgumentCaptor<Garden> gardenCaptor = ArgumentCaptor.forClass(Garden.class);
    then(gardenRepository).should().save(gardenCaptor.capture());

    // 2. 저장된 Garden 객체의 속성 검증
    Garden savedGarden = gardenCaptor.getValue();
    assertThat(savedGarden.getUser()).isEqualTo(user);
    assertThat(savedGarden.getSlotNumber()).isEqualTo(2); // 새 텃밭의 슬롯 번호는 2

    // 3. User 객체의 gardens 리스트 크기가 증가했는지 검증
    assertThat(user.getGardens()).hasSize(2);
  }

  @Test
  @DisplayName("실패 - 레벨이 부족할 때 예외가 발생한다")
  void unlockGarden_Fail_InsufficientLevel() {
    // given
    // 레벨 1이고, 텃밭을 1개 가지고 있는 사용자
    User user =
        User.builder()
            .id(1L)
            .level(1)
            .gardens(new ArrayList<>(List.of(Garden.builder().slotNumber(1).build())))
            .build();

    // when & then
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> gardenService.unlockGarden(user));

    assertThat(exception.getMessage()).isEqualTo("레벨이 부족하여 더 이상 텃밭을 잠금 해제할 수 없습니다.");
    then(gardenRepository).should(never()).save(any(Garden.class));
  }

  @Test
  @DisplayName("실패 - 최대 텃밭 개수(3개)에 도달했을 때 예외가 발생한다")
  void unlockGarden_Fail_MaxGardensReached() {
    // given
    // 레벨 4이지만, 텃밭을 이미 3개 가지고 있는 사용자
    User user =
        User.builder()
            .id(1L)
            .level(4)
            .gardens(
                new ArrayList<>(
                    List.of(
                        Garden.builder().slotNumber(1).build(),
                        Garden.builder().slotNumber(2).build(),
                        Garden.builder().slotNumber(3).build())))
            .build();

    // when & then
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> gardenService.unlockGarden(user));

    assertThat(exception.getMessage()).isEqualTo("텃밭은 최대 3개까지만 생성할 수 있습니다.");
    then(gardenRepository).should(never()).save(any(Garden.class));
  }
}
