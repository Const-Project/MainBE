package com.example.cp_main_be.domain.mission.quiz.service;

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
import com.example.cp_main_be.domain.mission.quiz.dto.QuizResponseDTO;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserDailyMission;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.repository.UserDailyMissionRepository;
import com.example.cp_main_be.domain.mission.user_daily_mission.service.UserDailyMissionService;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

  private final UserDailyMissionRepository userDailyMissionRepository;
  private final QuizRepository quizRepository;
  private final QuizOptionsRepository quizOptionsRepository;
  private final UserDailyMissionService userDailyMissionService;
  private final UserService userService;

  public QuizResponseDTO getQuiz(Long userDailyMissionId) {
    UserDailyMission userDailyMission = getUserDailyMission(userDailyMissionId);
    validateOwnership(userDailyMission);
    DailyMissionMaster dailyMissionMaster = userDailyMission.getDailyMissionMaster();
    validateQuizMissionType(dailyMissionMaster);
    Quiz quiz = getQuizByMissionId(dailyMissionMaster.getId());
    List<QuizOptions> quizOptions = quizOptionsRepository.findAllByQuizId(quiz.getId());

    // 정답 정보 제외하고 DTO 생성
    List<QuizResponseDTO.QuizOptionResponseDTO> optionDTOs =
        quizOptions.stream()
            .map(
                option ->
                    QuizResponseDTO.QuizOptionResponseDTO.builder()
                        .id(option.getId())
                        .text(option.getOptionText())
                        .build())
            .collect(Collectors.toList());

    return QuizResponseDTO.builder()
        .quizType(quiz.getQuizType())
        .quizQuestion(quiz.getQuizQuestion())
        .quizOptions(optionDTOs)
        .quizId(quiz.getId())
        .build();
  }

  public CompletedQuizResponseDTO getCompletedQuizResult(Long userDailyMissionId) {
    UserDailyMission userDailyMission = getUserDailyMission(userDailyMissionId);
    validateOwnership(userDailyMission);

    if (userDailyMission.getSelectedAnswerNumber() == null) {
      throw new CustomApiException(ErrorCode.INVALID_REQUEST, "아직 제출하지 않은 퀴즈입니다.");
    }

    LocalDate missionDate = userDailyMission.getCreatedAt().toLocalDate();
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    if (!missionDate.equals(today)) {
      throw new CustomApiException(ErrorCode.INVALID_REQUEST, "오늘의 퀴즈 결과만 조회할 수 있습니다.");
    }

    DailyMissionMaster dailyMissionMaster = userDailyMission.getDailyMissionMaster();
    validateQuizMissionType(dailyMissionMaster);
    Quiz quiz = getQuizByMissionId(dailyMissionMaster.getId());
    List<QuizOptions> quizOptions = quizOptionsRepository.findAllByQuizId(quiz.getId());

    Integer selectedAnswerNumber = userDailyMission.getSelectedAnswerNumber();

    List<CompletedQuizResponseDTO.CompletedQuizOptionResponseDTO> optionDTOs =
        quizOptions.stream()
            .map(
                option ->
                    CompletedQuizResponseDTO.CompletedQuizOptionResponseDTO.builder()
                        .id(option.getId())
                        .text(option.getOptionText())
                        .isAnswer(option.getOptionOrder() == quiz.getAnswerNumber())
                        .isSelected(option.getOptionOrder().equals(selectedAnswerNumber))
                        .optionOrder(option.getOptionOrder())
                        .build())
            .toList();

    return CompletedQuizResponseDTO.builder()
        .quizType(quiz.getQuizType())
        .quizQuestion(quiz.getQuizQuestion())
        .quizOptions(optionDTOs)
        .missionId(dailyMissionMaster.getId())
        .isCorrect(Boolean.TRUE.equals(userDailyMission.getIsQuizCorrect()))
        .selectedAnswerNumber(selectedAnswerNumber)
        .build();
  }

  // 퀴즈 제출 API
  public CompletedQuizResponseDTO summitQuizAnswer(
      QuizRequestDTO request, Long userDailyMissionId) {
    UserDailyMission userDailyMission =
        userDailyMissionRepository
            .findById(userDailyMissionId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.MISSION_NOT_FOUND));
    validateOwnership(userDailyMission);
    if (userDailyMission.getSelectedAnswerNumber() != null) {
      throw new CustomApiException(ErrorCode.INVALID_REQUEST, "이미 제출한 퀴즈입니다.");
    }
    DailyMissionMaster dailyMissionMaster = userDailyMission.getDailyMissionMaster();
    validateQuizMissionType(dailyMissionMaster);
    Quiz quiz = getQuizByMissionId(dailyMissionMaster.getId());
    List<QuizOptions> quizOptions = quizOptionsRepository.findAllByQuizId(quiz.getId());
    // 선택한 선지 정보 불러오기
    QuizOptions selectedQuizOption =
        quizOptions.stream()
            .filter(option -> option.getId().equals(request.getSelectedOptionId()))
            .findFirst()
            .orElseThrow(
                () -> new CustomApiException(ErrorCode.INVALID_REQUEST, "해당하는 선지를 찾을 수 없습니다."));
    // 퀴즈 정답 확인
    boolean isCorrect = selectedQuizOption.getOptionOrder() == quiz.getAnswerNumber();

    userDailyMission.setSelectedAnswerNumber(selectedQuizOption.getOptionOrder());
    userDailyMission.setIsQuizCorrect(isCorrect);
    userDailyMission.setCompleted(true);
    userDailyMission.setCompletedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));

    // 정답 정보 포함하여 DTO 생성
    List<CompletedQuizResponseDTO.CompletedQuizOptionResponseDTO> optionDTOs =
        quizOptions.stream()
            .map(
                option ->
                    CompletedQuizResponseDTO.CompletedQuizOptionResponseDTO.builder()
                        .id(option.getId())
                        .text(option.getOptionText())
                        .isAnswer(option.getOptionOrder() == quiz.getAnswerNumber()) // 정답 여부
                        .isSelected(option.getId().equals(request.getSelectedOptionId()))
                        .optionOrder(option.getOptionOrder())
                        .build())
            .toList();

    return CompletedQuizResponseDTO.builder()
        .quizType(quiz.getQuizType())
        .quizQuestion(quiz.getQuizQuestion())
        .quizOptions(optionDTOs)
        .missionId(dailyMissionMaster.getId())
        .isCorrect(isCorrect)
        .selectedAnswerNumber(selectedQuizOption.getOptionOrder())
        .build();
  }

  // 공통 메서드들
  private UserDailyMission getUserDailyMission(Long userDailyMissionId) {
    return userDailyMissionRepository
        .findById(userDailyMissionId)
        .orElseThrow(() -> new CustomApiException(ErrorCode.MISSION_NOT_FOUND));
  }

  private Quiz getQuizByMissionId(Long missionId) {
    return quizRepository
        .findByDailyMissionMaster_Id(missionId)
        .orElseThrow(() -> new CustomApiException(ErrorCode.QUIZ_NOT_FOUND));
  }

  private void validateOwnership(UserDailyMission userDailyMission) {
    User currentUser = userService.getCurrentUser();
    if (!userDailyMission.getUser().getId().equals(currentUser.getId())) {
      throw new CustomApiException(ErrorCode.ACCESS_DENIED);
    }
  }

  private void validateQuizMissionType(DailyMissionMaster dailyMissionMaster) {
    if (dailyMissionMaster.getMissionType() != MissionType.QUIZ) {
      throw new CustomApiException(ErrorCode.INVALID_REQUEST, "퀴즈 타입의 미션이 아닙니다.");
    }
  }
}
