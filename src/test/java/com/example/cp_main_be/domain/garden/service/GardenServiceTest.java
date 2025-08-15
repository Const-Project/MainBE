package com.example.cp_main_be.domain.garden.service;

import com.example.cp_main_be.domain.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.domain.repository.GardenRepository;
import com.example.cp_main_be.domain.garden.dto.GardenResponse;
import com.example.cp_main_be.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

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
}
