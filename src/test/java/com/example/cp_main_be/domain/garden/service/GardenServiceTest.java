package com.example.cp_main_be.domain.garden.service;

import com.example.cp_main_be.domain.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.domain.GardenBackground;
import com.example.cp_main_be.domain.garden.domain.repository.GardenBackgroundRepository;
import com.example.cp_main_be.domain.garden.domain.repository.GardenRepository;
import com.example.cp_main_be.domain.garden.dto.response.GardenBackgroundCandidateResponse;
import com.example.cp_main_be.domain.garden.dto.response.GardenBackgroundResponse;
import com.example.cp_main_be.domain.garden.dto.response.GardenResponse;
import com.example.cp_main_be.domain.member.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class GardenServiceTest {

  @Mock private GardenRepository gardenRepository;
  @Mock private GardenBackgroundRepository gardenBackgroundRepository;

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
    Garden garden = Garden.builder().user(user).slotNumber(1).waterCount(0).build();

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
    Garden garden = Garden.builder().user(user).slotNumber(1).sunlightCount(0).build();
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

  @Nested
  @DisplayName("텃밭 배경화면 조회(getGardenBackgroundImage) 테스트")
  class GetGardenBackgroundImageTest {

    @Test
    @DisplayName("성공 - 설정된 배경화면 URL을 정상적으로 반환한다")
    void getBackgroundImage_Success() {
      // given
      Long gardenId = 1L;
      String imageUrl = "https://example.com/bg.jpg";
      GardenBackground background =
          GardenBackground.builder().name("테스트 배경").imageUrl(imageUrl).build();
      Garden garden = Garden.builder().id(gardenId).gardenBackground(background).build();

      BDDMockito.given(gardenRepository.findById(gardenId))
          .willReturn(java.util.Optional.of(garden));

      // when
      GardenBackgroundResponse response = gardenService.getGardenBackgroundImage(gardenId);

      // then
      assertThat(response.getBackgroundImageUrl()).isEqualTo(imageUrl);
    }

    @Test
    @DisplayName("실패 - 텃밭이 존재하지 않으면 예외가 발생한다")
    void getBackgroundImage_Fail_GardenNotFound() {
      // given
      Long gardenId = 99L;
      BDDMockito.given(gardenRepository.findById(gardenId)).willReturn(java.util.Optional.empty());

      // when & then
      assertThrows(
          IllegalArgumentException.class, () -> gardenService.getGardenBackgroundImage(gardenId));
    }

    @Test
    @DisplayName("실패 - 배경화면이 설정되어 있지 않으면 예외가 발생한다")
    void getBackgroundImage_Fail_BackgroundNotSet() {
      // given
      Long gardenId = 1L;
      Garden garden = Garden.builder().id(gardenId).gardenBackground(null).build(); // 배경화면이 null

      BDDMockito.given(gardenRepository.findById(gardenId))
          .willReturn(java.util.Optional.of(garden));

      // when & then
      assertThrows(
          IllegalStateException.class, () -> gardenService.getGardenBackgroundImage(gardenId));
    }
  }

  @Nested
  @DisplayName("텃밭 배경화면 변경(updateGardenBackgroundImage) 테스트")
  class UpdateGardenBackgroundImageTest {

    @Test
    @DisplayName("성공 - 텃밭의 배경화면을 새로운 배경화면으로 변경한다")
    void updateBackgroundImage_Success() {
      // given
      Long gardenId = 1L;
      Long backgroundId = 10L;

      Garden garden = Garden.builder().id(gardenId).build();
      GardenBackground newBackground =
          GardenBackground.builder().id(backgroundId).name("새 배경").imageUrl("new_url").build();

      BDDMockito.given(gardenRepository.findById(gardenId))
          .willReturn(java.util.Optional.of(garden));
      BDDMockito.given(gardenBackgroundRepository.findById(backgroundId))
          .willReturn(java.util.Optional.of(newBackground));

      // when
      gardenService.updateGardenBackgroundImage(gardenId, backgroundId);

      // then
      // garden 객체의 배경화면이 newBackground로 변경되었는지 확인
      assertThat(garden.getGardenBackground()).isEqualTo(newBackground);
    }

    @Test
    @DisplayName("실패 - 변경하려는 배경화면이 존재하지 않으면 예외가 발생한다")
    void updateBackgroundImage_Fail_BackgroundNotFound() {
      // given
      Long gardenId = 1L;
      Long backgroundId = 99L; // 존재하지 않는 배경 ID

      Garden garden = Garden.builder().id(gardenId).build();

      BDDMockito.given(gardenRepository.findById(gardenId))
          .willReturn(java.util.Optional.of(garden));
      BDDMockito.given(gardenBackgroundRepository.findById(backgroundId))
          .willReturn(java.util.Optional.empty());

      // when & then
      assertThrows(
          IllegalArgumentException.class,
          () -> gardenService.updateGardenBackgroundImage(gardenId, backgroundId));
    }
  }

  @Nested
  @DisplayName("모든 배경화면 목록 조회(getAllBackgrounds) 테스트")
  class GetAllBackgroundsTest {

    @Test
    @DisplayName("성공 - 모든 배경화면 후보 목록을 DTO로 변환하여 반환한다")
    void getAllBackgrounds_Success() {
      // given
      List<GardenBackground> backgrounds =
          List.of(
              GardenBackground.builder().id(1L).name("봄").imageUrl("spring.jpg").build(),
              GardenBackground.builder().id(2L).name("겨울").imageUrl("winter.jpg").build());

      BDDMockito.given(gardenBackgroundRepository.findAll()).willReturn(backgrounds);

      // when
      List<GardenBackgroundCandidateResponse> responses = gardenService.getAllBackgrounds();

      // then
      assertThat(responses).hasSize(2);
      assertThat(responses.get(0).getName()).isEqualTo("봄");
      assertThat(responses.get(1).getImageUrl()).isEqualTo("winter.jpg");
    }

    @Test
    @DisplayName("성공 - 배경화면 후보가 없을 경우 빈 리스트를 반환한다")
    void getAllBackgrounds_Success_EmptyList() {
      // given
      BDDMockito.given(gardenBackgroundRepository.findAll()).willReturn(List.of());

      // when
      List<GardenBackgroundCandidateResponse> responses = gardenService.getAllBackgrounds();

      // then
      assertThat(responses).isNotNull();
      assertThat(responses).isEmpty();
    }
  }
}
