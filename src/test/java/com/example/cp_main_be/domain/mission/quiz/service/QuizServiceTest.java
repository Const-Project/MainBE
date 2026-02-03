package com.example.cp_main_be.domain.mission.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.domain.mission.daily_mission_master.MissionType;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import com.example.cp_main_be.domain.mission.quiz.domain.Quiz;
import com.example.cp_main_be.domain.mission.quiz.domain.QuizOptions;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizOptionsRepository;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizRepository;
import com.example.cp_main_be.domain.mission.quiz.dto.CompletedQuizResponseDTO;
import com.example.cp_main_be.domain.mission.quiz.dto.QuizRequestDTO;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserDailyMission;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.repository.UserDailyMissionRepository;
import com.example.cp_main_be.domain.mission.user_daily_mission.service.UserDailyMissionService;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

  @InjectMocks private QuizService quizService;

  @Mock private UserDailyMissionRepository userDailyMissionRepository;
  @Mock private QuizRepository quizRepository;
  @Mock private QuizOptionsRepository quizOptionsRepository;
  @Mock private UserDailyMissionService userDailyMissionService;
  @Mock private UserService userService;

  @Test
  @DisplayName("퀴즈 조회 실패 - 다른 사용자의 미션 접근")
  void getQuiz_Fail_AccessDenied() {
    Long userDailyMissionId = 100L;
    User currentUser = User.builder().id(1L).build();
    User owner = User.builder().id(2L).build();
    DailyMissionMaster dailyMissionMaster =
        DailyMissionMaster.builder().id(10L).missionType(MissionType.QUIZ).build();
    UserDailyMission userDailyMission = org.mockito.Mockito.mock(UserDailyMission.class);

    given(userDailyMissionRepository.findById(userDailyMissionId))
        .willReturn(Optional.of(userDailyMission));
    given(userDailyMission.getUser()).willReturn(owner);
    given(userDailyMission.getDailyMissionMaster()).willReturn(dailyMissionMaster);
    given(userService.getCurrentUser()).willReturn(currentUser);

    CustomApiException exception =
        assertThrows(CustomApiException.class, () -> quizService.getQuiz(userDailyMissionId));

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
    verifyNoInteractions(quizRepository, quizOptionsRepository);
  }

  @Test
  @DisplayName("퀴즈 제출 실패 - 퀴즈에 없는 선지 선택")
  void submitQuiz_Fail_SelectedOptionNotInQuiz() {
    Long userDailyMissionId = 200L;
    User currentUser = User.builder().id(1L).build();
    DailyMissionMaster dailyMissionMaster =
        DailyMissionMaster.builder().id(11L).missionType(MissionType.QUIZ).build();
    UserDailyMission userDailyMission = org.mockito.Mockito.mock(UserDailyMission.class);

    Quiz quiz =
        Quiz.builder().id(21L).dailyMissionMaster(dailyMissionMaster).answerNumber(2L).build();

    List<QuizOptions> quizOptions =
        List.of(
            QuizOptions.builder().id(11L).quiz(quiz).optionOrder(1).optionText("A").build(),
            QuizOptions.builder().id(12L).quiz(quiz).optionOrder(2).optionText("B").build());

    QuizRequestDTO request = new QuizRequestDTO();
    request.setSelectedOptionId(99L);

    given(userDailyMissionRepository.findById(userDailyMissionId))
        .willReturn(Optional.of(userDailyMission));
    given(userDailyMission.getUser()).willReturn(currentUser);
    given(userDailyMission.getDailyMissionMaster()).willReturn(dailyMissionMaster);
    given(userService.getCurrentUser()).willReturn(currentUser);
    given(quizRepository.findByDailyMissionMaster_Id(dailyMissionMaster.getId()))
        .willReturn(Optional.of(quiz));
    given(quizOptionsRepository.findAllByQuizId(quiz.getId())).willReturn(quizOptions);

    CustomApiException exception =
        assertThrows(
            CustomApiException.class,
            () -> quizService.summitQuizAnswer(request, userDailyMissionId));

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
  }

  @Test
  @DisplayName("퀴즈 제출 성공 - 정답 판정은 optionOrder 기준")
  void submitQuiz_Success_IsCorrectByOptionOrder() {
    Long userDailyMissionId = 300L;
    User currentUser = User.builder().id(1L).build();
    DailyMissionMaster dailyMissionMaster =
        DailyMissionMaster.builder().id(12L).missionType(MissionType.QUIZ).build();
    UserDailyMission userDailyMission = org.mockito.Mockito.mock(UserDailyMission.class);

    Quiz quiz =
        Quiz.builder().id(31L).dailyMissionMaster(dailyMissionMaster).answerNumber(2L).build();

    QuizOptions option1 =
        QuizOptions.builder().id(21L).quiz(quiz).optionOrder(1).optionText("A").build();
    QuizOptions option2 =
        QuizOptions.builder().id(22L).quiz(quiz).optionOrder(2).optionText("B").build();
    List<QuizOptions> quizOptions = List.of(option1, option2);

    QuizRequestDTO request = new QuizRequestDTO();
    request.setSelectedOptionId(22L);

    given(userDailyMissionRepository.findById(userDailyMissionId))
        .willReturn(Optional.of(userDailyMission));
    given(userDailyMission.getUser()).willReturn(currentUser);
    given(userDailyMission.getDailyMissionMaster()).willReturn(dailyMissionMaster);
    given(userService.getCurrentUser()).willReturn(currentUser);
    given(quizRepository.findByDailyMissionMaster_Id(dailyMissionMaster.getId()))
        .willReturn(Optional.of(quiz));
    given(quizOptionsRepository.findAllByQuizId(quiz.getId())).willReturn(quizOptions);

    CompletedQuizResponseDTO response = quizService.summitQuizAnswer(request, userDailyMissionId);

    assertThat(response.getIsCorrect()).isTrue();
    assertThat(response.getSelectedAnswerNumber()).isEqualTo(2);
  }
}
