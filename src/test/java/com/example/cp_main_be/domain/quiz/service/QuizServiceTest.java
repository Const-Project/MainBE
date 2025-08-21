package com.example.cp_main_be.domain.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizOptionsRepository;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizRepository;
import com.example.cp_main_be.domain.mission.quiz.service.QuizService;
import com.example.cp_main_be.domain.mission.user_daily_mission.repository.UserDailyMissionRepository;
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

  // DailyMissionMastersRepository는 직접 호출되지 않으므로 Mock 객체가 필요 없습니다.

  //  @Test
  //  @DisplayName("퀴즈 조회 성공")
  //  void getQuiz_Success() {
  //    // Given (준비)
  //    Long userDailyMissionId = 1L;
  //    Long dailyMissionMasterId = 101L;
  //    Long quizId = 201L;
  //
  //    // 연관 데이터 설정
  //    DailyMissionMaster dailyMissionMaster =
  //        DailyMissionMaster.builder().id(dailyMissionMasterId).title("오늘의 퀴즈 미션").build();
  //
  //    UserDailyMission userDailyMission =
  //        UserDailyMission.builder()
  //            .id(userDailyMissionId)
  //            .dailyMissionMaster(dailyMissionMaster)
  //            .build();
  //
  //    Quiz quiz =
  //        Quiz.builder()
  //            .id(quizId)
  //            .dailyMissionMaster(dailyMissionMaster)
  //            .quizType(QuizType.MULTI_CHOICE)
  //            .quizQuestion("다음 중 가장 큰 동물은?")
  //            .build();
  //
  //    List<QuizOptions> quizOptions =
  //        List.of(
  //            QuizOptions.builder().id(301L).quiz(quiz).optionText("코끼리").isCorrect(true).build(),
  //
  // QuizOptions.builder().id(302L).quiz(quiz).optionText("고양이").isCorrect(false).build());
  //
  //    // Mock 객체 동작 정의
  //    given(userDailyMissionRepository.findById(userDailyMissionId))
  //        .willReturn(Optional.of(userDailyMission));
  //    given(quizRepository.findByDailyMissionMaster_Id(dailyMissionMasterId))
  //        .willReturn(Optional.of(quiz));
  //    given(quizOptionsRepository.findAllByQuizId(quizId)).willReturn(quizOptions);
  //
  //    // When (실행)
  //    QuizResponseDTO result = quizService.getQuiz(userDailyMissionId);
  //
  //    // Then (검증)
  //    assertThat(result).isNotNull();
  //    assertThat(result.getQuizQuestion()).isEqualTo("다음 중 가장 큰 동물은?");
  //    assertThat(result.getQuizType()).isEqualTo(QuizType.MULTI_CHOICE);
  //    assertThat(result.getMissionId()).isEqualTo(dailyMissionMasterId);
  //    assertThat(result.getQuizOptions()).hasSize(2);
  //    assertThat(result.getQuizOptions().get(0).getText()).isEqualTo("코끼리");
  //
  //    // 메소드 호출 횟수 검증
  //    verify(userDailyMissionRepository, times(1)).findById(userDailyMissionId);
  //    verify(quizRepository, times(1)).findByDailyMissionMaster_Id(dailyMissionMasterId);
  //    verify(quizOptionsRepository, times(1)).findAllByQuizId(quizId);
  //  }

  @Test
  @DisplayName("퀴즈 조회 실패 - 해당 ID의 미션 없음")
  void getQuiz_Fail_UserDailyMissionNotFound() {
    // Given
    Long userDailyMissionId = 999L; // 존재하지 않는 ID
    given(userDailyMissionRepository.findById(userDailyMissionId)).willReturn(Optional.empty());

    // When & Then
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              quizService.getQuiz(userDailyMissionId);
            });

    assertThat(exception.getMessage()).isEqualTo("해당 ID를 갖는 미션이 존재하지 않습니다.");
    verify(userDailyMissionRepository, times(1)).findById(userDailyMissionId);
    // 미션을 찾지 못했으므로 다른 repository는 호출되지 않아야 함
    verifyNoInteractions(quizRepository, quizOptionsRepository);
  }

  //  @Test
  //  @DisplayName("퀴즈 조회 실패 - 미션에 해당하는 퀴즈 없음")
  //  void getQuiz_Fail_QuizNotFound() {
  //    // Given
  //    Long userDailyMissionId = 1L;
  //    Long dailyMissionMasterId = 101L;
  //
  //    DailyMissionMaster dailyMissionMaster =
  //        DailyMissionMaster.builder().id(dailyMissionMasterId).build();
  //    UserDailyMission userDailyMission =
  //        UserDailyMission.builder()
  //            .id(userDailyMissionId)
  //            .dailyMissionMaster(dailyMissionMaster)
  //            .build();
  //
  //    given(userDailyMissionRepository.findById(userDailyMissionId))
  //        .willReturn(Optional.of(userDailyMission));
  //    given(quizRepository.findByDailyMissionMaster_Id(dailyMissionMasterId))
  //        .willReturn(Optional.empty()); // 퀴즈가 없음
  //
  //    // When & Then
  //    RuntimeException exception =
  //        assertThrows(
  //            RuntimeException.class,
  //            () -> {
  //              quizService.getQuiz(userDailyMissionId);
  //            });
  //
  //    assertThat(exception.getMessage()).isEqualTo("퀴즈가 존재하지 않습니다.");
  //    verify(userDailyMissionRepository, times(1)).findById(userDailyMissionId);
  //    verify(quizRepository, times(1)).findByDailyMissionMaster_Id(dailyMissionMasterId);
  //    // 퀴즈를 찾지 못했으므로 옵션 repository는 호출되지 않아야 함
  //    verifyNoInteractions(quizOptionsRepository);
  //  }
}
