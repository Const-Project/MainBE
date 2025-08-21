package com.example.cp_main_be.domain.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.example.cp_main_be.domain.admin.dto.AdminRequestDTO;
import com.example.cp_main_be.domain.garden.plant_masters.domain.PlantMasters;
import com.example.cp_main_be.domain.garden.plant_masters.domain.repository.PlantMasterRepository;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.UserStatus;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.domain.mission.daily_keywords.domain.DailyKeywords;
import com.example.cp_main_be.domain.mission.daily_keywords.domain.repository.DailyKeywordsRepository;
import com.example.cp_main_be.domain.mission.daily_mission_master.MissionType;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.repository.DailyMissionMasterRepository;
import com.example.cp_main_be.domain.mission.quiz.domain.QuizOptions;
import com.example.cp_main_be.domain.reports.domain.Reports;
import com.example.cp_main_be.domain.reports.domain.repository.ReportRepository;
import com.example.cp_main_be.domain.reports.enums.ReportStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

  @InjectMocks private AdminService adminService;

  @Mock private DailyMissionMasterRepository dailyMissionMasterRepository;

  @Mock private DailyKeywordsRepository dailyKeywordsRepository;

  @Mock private PlantMasterRepository plantMasterRepository;

  @Mock private UserService userService;

  @Mock private ReportRepository reportRepository;

  @Test
  @DisplayName("일일 미션 마스터 생성 성공")
  void createDailyMissionMasters_Success() {
    // Given
    AdminRequestDTO.CreateMissionRequestDTO requestDTO =
        AdminRequestDTO.CreateMissionRequestDTO.builder()
            .title("새 미션")
            .description("미션 설명")
            .content("미션 내용")
            .missionType(MissionType.QUIZ)
            .rewardPoints(100L)
            .build();

    DailyMissionMaster mission = DailyMissionMaster.builder().id(1L).title("새 미션").build();
    given(dailyMissionMasterRepository.save(any(DailyMissionMaster.class))).willReturn(mission);

    // When
    DailyMissionMaster result = adminService.createDailyMissionMasters(requestDTO);

    // Then
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getTitle()).isEqualTo("새 미션");
    verify(dailyMissionMasterRepository, times(1)).save(any(DailyMissionMaster.class));
  }

  @Test
  @DisplayName("일일 미션 마스터 수정 성공")
  void updateDailyMissionMasters_Success() {
    // Given
    Long missionId = 1L;
    AdminRequestDTO.UpdateMissionRequestDTO requestDTO =
        AdminRequestDTO.UpdateMissionRequestDTO.builder().build();
    requestDTO.setTitle("수정된 미션 제목");

    DailyMissionMaster existingMission =
        DailyMissionMaster.builder().id(missionId).title("원본 제목").build();
    given(dailyMissionMasterRepository.findById(missionId))
        .willReturn(Optional.of(existingMission));

    // When
    DailyMissionMaster result = adminService.updateDailyMissionMasters(requestDTO, missionId);

    // Then
    assertThat(result.getTitle()).isEqualTo("수정된 미션 제목");
    verify(dailyMissionMasterRepository, times(1)).findById(missionId);
  }

  @Test
  @DisplayName("일일 미션 마스터 수정 실패 - 미션 없음")
  void updateDailyMissionMasters_Fail_NotFound() {
    // Given
    Long missionId = 999L;
    AdminRequestDTO.UpdateMissionRequestDTO requestDTO =
        AdminRequestDTO.UpdateMissionRequestDTO.builder().build();
    given(dailyMissionMasterRepository.findById(missionId)).willReturn(Optional.empty());

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          adminService.updateDailyMissionMasters(requestDTO, missionId);
        });
  }

  @Test
  @DisplayName("오늘의 키워드 생성 성공")
  void createDailyKeywords_Success() {
    // Given
    AdminRequestDTO.CreateKeywordRequestDTO requestDTO =
        AdminRequestDTO.CreateKeywordRequestDTO.builder().build();
    requestDTO.setKeyword("테스트 키워드");
    requestDTO.setKeywordDate(LocalDateTime.now());

    DailyKeywords keyword = new DailyKeywords();
    keyword.setId(1L);
    keyword.setKeyword("테스트 키워드");

    given(dailyKeywordsRepository.save(any(DailyKeywords.class))).willReturn(keyword);

    // When
    DailyKeywords result = adminService.createDailyKeywords(requestDTO);

    // Then
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getKeyword()).isEqualTo("테스트 키워드");
    verify(dailyKeywordsRepository, times(1)).save(any(DailyKeywords.class));
  }

  @Test
  @DisplayName("모든 유저 조회 성공")
  void getUsers_Success() {
    // Given
    User user1 = User.builder().id(1L).username("유저1").build();
    User user2 = User.builder().id(2L).username("유저2").build();
    given(userService.findAllUsers()).willReturn(List.of(user1, user2));

    // When
    List<User> users = adminService.getUsers();

    // Then
    assertThat(users).hasSize(2);
    assertThat(users.get(0).getUsername()).isEqualTo("유저1");
    verify(userService, times(1)).findAllUsers();
  }

  @Test
  @DisplayName("유저 상태 변경 성공")
  void changeUserStatus_Success() {
    // Given
    Long userId = 1L;
    AdminRequestDTO.ChangeUserStatusRequestDTO requestDTO =
        AdminRequestDTO.ChangeUserStatusRequestDTO.builder().build();
    requestDTO.setUserStatus(UserStatus.INACTIVE);

    User user = User.builder().id(userId).status(UserStatus.ACTIVE).build();
    given(userService.findUserById(userId)).willReturn(user);

    // When
    User result = adminService.chageUserStatus(userId, requestDTO);

    // Then
    assertThat(result.getStatus()).isEqualTo(UserStatus.INACTIVE);
    verify(userService, times(1)).findUserById(userId);
  }

  @Test
  @DisplayName("퀴즈 옵션 생성 성공")
  void createQuizOption_Success() {
    // Given
    Long missionMasterId = 1L;
    AdminRequestDTO.CreateQuizRequestDTO requestDTO =
        AdminRequestDTO.CreateQuizRequestDTO.builder()
            .missionMasterId(missionMasterId)
            .optionText("정답")
            .optionOrder(1)
            .isCorrect(true)
            .build();

    DailyMissionMaster missionMaster = DailyMissionMaster.builder().id(missionMasterId).build();
    given(dailyMissionMasterRepository.findById(missionMasterId))
        .willReturn(Optional.of(missionMaster));

    // When
    QuizOptions result = adminService.createQuizOption(requestDTO);

    // Then
    assertThat(result.getOptionText()).isEqualTo("정답");
    assertThat(result.isCorrect()).isTrue();
    verify(dailyMissionMasterRepository, times(1)).findById(missionMasterId);
  }

  @Test
  @DisplayName("퀴즈 옵션 생성 실패 - 미션 없음")
  void createQuizOption_Fail_MissionNotFound() {
    // Given
    Long missionMasterId = 999L;
    AdminRequestDTO.CreateQuizRequestDTO requestDTO =
        AdminRequestDTO.CreateQuizRequestDTO.builder().missionMasterId(missionMasterId).build();
    given(dailyMissionMasterRepository.findById(missionMasterId)).willReturn(Optional.empty());

    // When & Then
    assertThrows(
        IllegalStateException.class,
        () -> {
          adminService.createQuizOption(requestDTO);
        });
  }

  @Test
  @DisplayName("새 식물 생성 성공")
  void createNewPlant_Success() {
    // Given
    AdminRequestDTO.CreatePlantMasterRequestDTO requestDTO =
        AdminRequestDTO.CreatePlantMasterRequestDTO.builder()
            .plantName("새 식물")
            .plantType("허브")
            .build();

    PlantMasters plant = PlantMasters.builder().id(1L).plantName("새 식물").build();
    given(plantMasterRepository.save(any(PlantMasters.class))).willReturn(plant);

    // When
    PlantMasters result = adminService.createNewPlant(requestDTO);

    // Then
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getPlantName()).isEqualTo("새 식물");
    verify(plantMasterRepository, times(1)).save(any(PlantMasters.class));
  }

  @Test
  @DisplayName("식물 정보 수정 성공")
  void updatePlantMasters_Success() {
    // Given
    Long plantId = 1L;
    AdminRequestDTO.UpdatePlantMasterRequestDTO requestDTO =
        AdminRequestDTO.UpdatePlantMasterRequestDTO.builder().build();
    requestDTO.setPlantName("수정된 식물 이름");

    PlantMasters existingPlant = PlantMasters.builder().id(plantId).plantName("원본 식물 이름").build();
    given(plantMasterRepository.findById(plantId)).willReturn(Optional.of(existingPlant));

    // When
    PlantMasters result = adminService.updatePlantMasters(plantId, requestDTO);

    // Then
    assertThat(result.getPlantName()).isEqualTo("수정된 식물 이름");
    verify(plantMasterRepository, times(1)).findById(plantId);
  }

  @Test
  @DisplayName("식물 정보 수정 실패 - 식물 없음")
  void updatePlantMasters_Fail_NotFound() {
    // Given
    Long plantId = 999L;
    AdminRequestDTO.UpdatePlantMasterRequestDTO requestDTO =
        AdminRequestDTO.UpdatePlantMasterRequestDTO.builder().build();
    given(plantMasterRepository.findById(plantId)).willReturn(Optional.empty());

    // When & Then
    assertThrows(
        IllegalStateException.class,
        () -> {
          adminService.updatePlantMasters(plantId, requestDTO);
        });
  }

  @Test
  @DisplayName("모든 신고 조회 성공")
  void getAllReports_Success() {
    // Given
    Reports report1 = Reports.builder().id(1L).additionalComment("신고 내용1").build();
    Reports report2 = Reports.builder().id(2L).additionalComment("신고 내용2").build();
    given(reportRepository.findAll()).willReturn(List.of(report1, report2));

    // When
    List<Reports> reports = adminService.getAllReports();

    // Then
    assertThat(reports).hasSize(2);
    verify(reportRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("신고 상태 업데이트 성공")
  void updateReportStatus_Success() {
    // Given
    Long reportId = 1L;
    ReportStatus newStatus = ReportStatus.RESOLVED;
    Reports report = Reports.builder().id(reportId).status(ReportStatus.PENDING).build();
    given(reportRepository.findById(reportId)).willReturn(Optional.of(report));

    // When
    Reports result = adminService.updateReportStatus(reportId, newStatus);

    // Then
    assertThat(result.getStatus()).isEqualTo(ReportStatus.RESOLVED);
    verify(reportRepository, times(1)).findById(reportId);
  }

  @Test
  @DisplayName("신고 상태 업데이트 실패 - 신고 없음")
  void updateReportStatus_Fail_NotFound() {
    // Given
    Long reportId = 999L;
    given(reportRepository.findById(reportId)).willReturn(Optional.empty());

    // When & Then
    assertThrows(
        RuntimeException.class,
        () -> {
          adminService.updateReportStatus(reportId, ReportStatus.RESOLVED);
        });
  }
}
