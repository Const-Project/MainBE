// package com.example.cp_main_be.domain.garden.service;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertThrows;
//
// import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
// import com.example.cp_main_be.domain.avatar.avatar.domain.AvatarMaster;
// import com.example.cp_main_be.domain.avatar.avatar.domain.repository.AvatarMasterRepository;
// import com.example.cp_main_be.domain.avatar.avatar.domain.repository.AvatarRepository;
// import com.example.cp_main_be.domain.garden.garden.domain.Garden;
// import com.example.cp_main_be.domain.garden.garden.domain.GardenBackground;
// import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenBackgroundRepository;
// import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenRepository;
// import com.example.cp_main_be.domain.garden.garden.dto.response.GardenResponse;
// import com.example.cp_main_be.domain.garden.garden.service.GardenService;
// import com.example.cp_main_be.domain.member.user.domain.User;
// import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
// import com.example.cp_main_be.domain.mission.wishTree.WishTree;
// import com.example.cp_main_be.domain.mission.wishTree.WishTreeRepository;
// import com.example.cp_main_be.domain.mission.wishTree.WishTreeService;
// import com.example.cp_main_be.global.common.CustomApiException;
// import com.example.cp_main_be.global.common.ErrorCode;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
// @DataJpaTest // JPA 관련 컴포넌트만 테스트하기 위한 어노테이션
// class GardenServiceTest {
//
//  // @DataJpaTest 환경에서는 실제 Repository Bean들이 주입됩니다.
//  @Autowired private GardenRepository gardenRepository;
//  @Autowired private UserRepository userRepository;
//  @Autowired private AvatarRepository avatarRepository;
//  @Autowired private AvatarMasterRepository avatarMasterRepository;
//  @Autowired private GardenBackgroundRepository gardenBackgroundRepository;
//  @Autowired private WishTreeRepository wishTreeRepository;
//
//  // 테스트 대상인 Service는 Bean으로 등록되지 않으므로, 수동으로 생성합니다.
//  private GardenService gardenService;
//  private WishTreeService wishTreeService;
//
//  @BeforeEach
//  void setUp() {
//    // WishTreeService는 ApplicationEventPublisher도 의존할 수 있으나, 이 테스트에서는 사용되지 않으므로 null을 전달합니다.
//    // 실제 WishTreeService의 생성자에 따라 조정이 필요할 수 있습니다.
//    wishTreeService = new WishTreeService(wishTreeRepository, null, null);
//
//    gardenService =
//        new GardenService(
//            wishTreeService, // [수정] sunlightGarden 테스트에 필요한 WishTreeService를 주입합니다.
//            gardenRepository,
//            null, // userService
//            null, // eventPublisher
//            gardenBackgroundRepository,
//            avatarRepository,
//            userRepository,
//            null, // friendWateringLogRepository
//            wishTreeRepository);
//  }
//
//  @Test
//  @DisplayName("텃밭 ID로 조회 성공 시 createdAt과 updatedAt은 null이 아니다")
//  void findGardenById_Success_AuditingFieldsAreNotNull() {
//    // Given: 테스트에 필요한 데이터를 미리 설정합니다.
//    User user = userRepository.save(User.builder().nickname("테스트유저").build());
//    GardenBackground background =
//        gardenBackgroundRepository.save(
//            GardenBackground.builder().name("기본 배경").imageUrl("bg.url").build());
//    AvatarMaster master =
// avatarMasterRepository.save(AvatarMaster.builder().defaultImageUrl("avatar.url").build());
//    Avatar avatar =
//        avatarRepository.save(
//            Avatar.builder().user(user).nickname("내 아바타").avatarMaster(master).build());
//
//    Garden garden =
//        Garden.builder()
//            .user(user)
//            .slotNumber(1)
//            .avatar(avatar)
//            .gardenBackground(background)
//            .isLocked(false)
//            .build();
//
//    // When: 엔티티를 저장하면 JPA Auditing 기능이 createdAt과 updatedAt을 자동으로 채워줍니다.
//    Garden savedGarden = gardenRepository.save(garden);
//    Long gardenId = savedGarden.getId();
//
//    // When: 테스트할 메서드를 호출합니다.
//    GardenResponse response = gardenService.findGardenById(gardenId);
//
//    // Then: 결과를 검증합니다.
//    assertThat(response).isNotNull();
//    assertThat(response.getId()).isEqualTo(gardenId);
//
//    // And: 가장 중요한 부분인 createdAt, updatedAt이 null이 아닌지 확인합니다.
//    assertThat(response.getCreatedAt()).isNotNull();
//    assertThat(response.getUpdatedAt()).isNotNull();
//  }
//
//  @Test
//  @DisplayName("존재하지 않는 텃밭 ID로 조회 시 예외 발생")
//  void findGardenById_Fail_WhenGardenNotFound() {
//    // Given: 존재하지 않는 ID를 준비합니다.
//    Long nonExistentGardenId = 999L;
//
//    // When & Then: 예외가 발생하는지, 그리고 예외 메시지가 올바른지 확인합니다.
//    IllegalArgumentException exception =
//        assertThrows(
//            IllegalArgumentException.class,
//            () -> {
//              gardenService.findGardenById(nonExistentGardenId);
//            });
//
//    assertThat(exception.getMessage()).isEqualTo("Garden not found");
//  }
//
//     @Test
//     @DisplayName("햇빛 주기 실패 - 하루에 한 번만 가능")
//     void sunlightGarden_Fail_WhenAlreadyGivenToday() {
//         // Given: 테스트 데이터 설정
//        User user = userRepository.save(User.builder().nickname("햇빛테스터").experience(0L).build());
//        wishTreeRepository.save(WishTree.builder().user(user).build()); // [수정] 햇빛주기 시 포인트를 받을
// 소원나무를 생성합니다.
//         Garden garden =
//                 gardenRepository.save(
//                         Garden.builder().user(user).slotNumber(1).isLocked(false).build());
//         Long userId = user.getId();
//         Long gardenId = garden.getId();
//
//         // When: 첫 번째 햇빛 주기는 성공해야 합니다.
//         gardenService.sunlightGarden(userId, gardenId);
//
//         // Then: 두 번째 햇빛 주기는 CustomApiException을 발생시켜야 합니다.
//         CustomApiException exception =
//                 assertThrows(
//                         CustomApiException.class,
//                         () -> {
//                             gardenService.sunlightGarden(userId, gardenId);
//                         });
//
//         // And: 발생한 예외의 에러 코드가 SUNLIGHT_COOL_DOWN인지 확인합니다.
//         assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SUNLIGHT_COOL_DOWN);
//     }
// }
