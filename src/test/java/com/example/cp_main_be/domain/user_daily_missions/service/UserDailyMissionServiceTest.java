package com.example.cp_main_be.domain.user_daily_missions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import com.example.cp_main_be.domain.mission.daily_mission_master.dto.response.DailyMissionResponseDTO;
import com.example.cp_main_be.domain.mission.quiz.domain.Quiz;
import com.example.cp_main_be.domain.mission.quiz.domain.QuizOptions;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizOptionsRepository;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizRepository;
import com.example.cp_main_be.domain.mission.quiz.dto.QuizRequestDTO;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserDailyMission;
import com.example.cp_main_be.domain.mission.user_daily_mission.repository.UserDailyMissionRepository;
import com.example.cp_main_be.domain.mission.user_daily_mission.service.UserDailyMissionService;
import com.example.cp_main_be.global.infra.S3Uploader;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class UserDailyMissionServiceTest {

  @InjectMocks private UserDailyMissionService userDailyMissionService;

  @Mock private UserDailyMissionRepository userDailyMissionRepository;

  @Mock private S3Uploader s3Uploader;

  @Mock private QuizOptionsRepository quizOptionsRepository;

  @Mock private QuizRepository quizRepository;

  // DailyMissionMastersRepository는 getDailyMissions 메소드에서만 사용되지만,
  // 해당 메소드는 UserDailyMissions를 통해 DailyMissionMasters를 가져오므로 직접적인 Mocking은 필요하지 않습니다.

  @Test
  @DisplayName("일일 미션 목록 조회 성공")
  void getDailyMissions_Success() {
    // Given (준비)
    Long userId = 1L;
    DailyMissionMaster missionMaster1 = DailyMissionMaster.builder().id(101L).title("미션 1").build();
    DailyMissionMaster missionMaster2 = DailyMissionMaster.builder().id(102L).title("미션 2").build();

    UserDailyMission userMission1 =
        UserDailyMission.builder().id(1L).dailyMissionMaster(missionMaster1).build();
    UserDailyMission userMission2 =
        UserDailyMission.builder().id(2L).dailyMissionMaster(missionMaster2).build();

    List<UserDailyMission> missions = List.of(userMission1, userMission2);

    given(userDailyMissionRepository.findAllByUserId(userId)).willReturn(missions);

    // When (실행)
    DailyMissionResponseDTO result = userDailyMissionService.getDailyMissions(userId);

    // Then (검증)
    assertThat(result).isNotNull();
    assertThat(result.getTodayMissions()).hasSize(2);
    assertThat(result.getTodayMissions().get(0).getMissionTitle()).isEqualTo("미션 1");
    assertThat(result.getTodayMissions().get(1).getMissionTitle()).isEqualTo("미션 2");

    verify(userDailyMissionRepository, times(1)).findAllByUserId(userId);
  }

  @Test
  @DisplayName("일일 미션 완료(삭제) 성공")
  void completeDailyMission_Success() {
    // Given
    Long dailyMissionId = 1L;
    UserDailyMission mission = UserDailyMission.builder().id(dailyMissionId).build();
    given(userDailyMissionRepository.findById(dailyMissionId)).willReturn(Optional.of(mission));

    // When
    userDailyMissionService.completeDailyMission(dailyMissionId);

    // Then
    verify(userDailyMissionRepository, times(1)).findById(dailyMissionId);
    verify(userDailyMissionRepository, times(1)).delete(mission);
  }

  @Test
  @DisplayName("일일 미션 완료(삭제) 실패 - 미션 없음")
  void completeDailyMission_Fail_MissionNotFound() {
    // Given
    Long dailyMissionId = 1L;
    given(userDailyMissionRepository.findById(dailyMissionId)).willReturn(Optional.empty());

    // When & Then
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              userDailyMissionService.completeDailyMission(dailyMissionId);
            });

    assertThat(exception.getMessage()).isEqualTo("미션을 찾을 수 없습니다.");
    verify(userDailyMissionRepository, times(1)).findById(dailyMissionId);
    verify(userDailyMissionRepository, times(0)).delete(any());
  }

  @Test
  @DisplayName("일일 미션 사진 업로드 성공")
  void uploadPictureForDailyMission_Success() {
    // Given
    Long userDailyMissionId = 1L;
    String expectedImageUrl = "http://s3.com/mission-images/test.jpg";
    MultipartFile mockFile =
        new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
    UserDailyMission mission = UserDailyMission.builder().id(userDailyMissionId).build();

    given(userDailyMissionRepository.findById(userDailyMissionId)).willReturn(Optional.of(mission));
    given(s3Uploader.upload(mockFile, "mission-images")).willReturn(expectedImageUrl);

    // When
    String imageUrl =
        userDailyMissionService.uploadPictureForDailyMission(userDailyMissionId, mockFile);

    // Then
    assertThat(imageUrl).isEqualTo(expectedImageUrl);
    assertThat(mission.getDailyMissionImage()).isNotNull();
    assertThat(mission.getDailyMissionImage().getImageUrl()).isEqualTo(expectedImageUrl);
    assertThat(mission.isCompleted()).isTrue();

    verify(s3Uploader, times(1)).upload(mockFile, "mission-images");
    verify(userDailyMissionRepository, times(1)).findById(userDailyMissionId);
  }

  @Test
  @DisplayName("일일 미션 사진 업로드 실패 - 미션 없음")
  void uploadPictureForDailyMission_Fail_MissionNotFound() {
    // Given
    Long userDailyMissionId = 1L;
    MultipartFile mockFile =
        new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
    given(userDailyMissionRepository.findById(userDailyMissionId)).willReturn(Optional.empty());

    // When & Then
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              userDailyMissionService.uploadPictureForDailyMission(userDailyMissionId, mockFile);
            });

    assertThat(exception.getMessage()).isEqualTo("미션을 찾을 수 없습니다.");
    verify(s3Uploader, times(0)).upload(any(MultipartFile.class), anyString());
  }

  @Test
  @DisplayName("퀴즈 정답 제출 - 정답")
  void summitAnswer_Correct() {
    // Given
    Long userDailyMissionId = 1L;
    Long dailyMissionMasterId = 101L;
    Long quizId = 201L;
    int correctAnswerNumber = 2;

    QuizRequestDTO request = new QuizRequestDTO();
    request.setAnswerNumber(correctAnswerNumber);

    DailyMissionMaster missionMaster =
        DailyMissionMaster.builder().id(dailyMissionMasterId).build();
    UserDailyMission userMission =
        UserDailyMission.builder().id(userDailyMissionId).dailyMissionMaster(missionMaster).build();
    Quiz quiz = Quiz.builder().id(quizId).dailyMissionMaster(missionMaster).build();

    List<QuizOptions> options =
        List.of(
            QuizOptions.builder().id(301L).quiz(quiz).optionOrder(1).isCorrect(false).build(),
            QuizOptions.builder().id(302L).quiz(quiz).optionOrder(2).isCorrect(true).build(),
            QuizOptions.builder().id(303L).quiz(quiz).optionOrder(3).isCorrect(false).build());

    given(userDailyMissionRepository.findById(userDailyMissionId))
        .willReturn(Optional.of(userMission));
    given(quizRepository.findByDailyMissionMasters_Id(dailyMissionMasterId))
        .willReturn(Optional.of(quiz));
    given(quizOptionsRepository.findAllByQuizId(quizId)).willReturn(options);

    // When
    Boolean result = userDailyMissionService.summitAnswer(request, userDailyMissionId);

    // Then
    assertThat(result).isTrue();
    verify(userDailyMissionRepository, times(1)).findById(userDailyMissionId);
    verify(quizRepository, times(1)).findByDailyMissionMasters_Id(dailyMissionMasterId);
    verify(quizOptionsRepository, times(1)).findAllByQuizId(quizId);
  }

  @Test
  @DisplayName("퀴즈 정답 제출 - 오답")
  void summitAnswer_Incorrect() {
    // Given
    Long userDailyMissionId = 1L;
    Long dailyMissionMasterId = 101L;
    Long quizId = 201L;
    int incorrectAnswerNumber = 1;

    QuizRequestDTO request = new QuizRequestDTO();
    request.setAnswerNumber(incorrectAnswerNumber);

    DailyMissionMaster missionMaster =
        DailyMissionMaster.builder().id(dailyMissionMasterId).build();
    UserDailyMission userMission =
        UserDailyMission.builder().id(userDailyMissionId).dailyMissionMaster(missionMaster).build();
    Quiz quiz = Quiz.builder().id(quizId).dailyMissionMaster(missionMaster).build();

    List<QuizOptions> options =
        List.of(
            QuizOptions.builder().id(301L).quiz(quiz).optionOrder(1).isCorrect(false).build(),
            QuizOptions.builder().id(302L).quiz(quiz).optionOrder(2).isCorrect(true).build());

    given(userDailyMissionRepository.findById(userDailyMissionId))
        .willReturn(Optional.of(userMission));
    given(quizRepository.findByDailyMissionMasters_Id(dailyMissionMasterId))
        .willReturn(Optional.of(quiz));
    given(quizOptionsRepository.findAllByQuizId(quizId)).willReturn(options);

    // When
    Boolean result = userDailyMissionService.summitAnswer(request, userDailyMissionId);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("퀴즈 정답 제출 실패 - 퀴즈 없음")
  void summitAnswer_Fail_QuizNotFound() {
    // Given
    Long userDailyMissionId = 1L;
    Long dailyMissionMasterId = 101L;
    QuizRequestDTO request = new QuizRequestDTO();
    request.setAnswerNumber(1);

    DailyMissionMaster missionMaster =
        DailyMissionMaster.builder().id(dailyMissionMasterId).build();
    UserDailyMission userMission =
        UserDailyMission.builder().id(userDailyMissionId).dailyMissionMaster(missionMaster).build();

    given(userDailyMissionRepository.findById(userDailyMissionId))
        .willReturn(Optional.of(userMission));
    given(quizRepository.findByDailyMissionMasters_Id(dailyMissionMasterId))
        .willReturn(Optional.empty());

    // When & Then
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              userDailyMissionService.summitAnswer(request, userDailyMissionId);
            });

    assertThat(exception.getMessage()).isEqualTo("퀴즈가 존재하지 않습니다.");
  }
}
