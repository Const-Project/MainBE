package com.example.cp_main_be.domain.mission.quiz.service;

import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import com.example.cp_main_be.domain.mission.quiz.domain.Quiz;
import com.example.cp_main_be.domain.mission.quiz.domain.QuizOptions;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizOptionsRepository;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizRepository;
import com.example.cp_main_be.domain.mission.quiz.dto.CompletedQuizResponseDTO;
import com.example.cp_main_be.domain.mission.quiz.dto.QuizRequestDTO;
import com.example.cp_main_be.domain.mission.quiz.dto.QuizResponseDTO;
import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserDailyMission;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserQuizMission;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.repository.UserDailyMissionRepository;
import com.example.cp_main_be.domain.mission.user_daily_mission.service.UserDailyMissionService;
import com.example.cp_main_be.global.exception.QuizNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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

  public QuizResponseDTO getQuiz(Long userDailyMissionId) {
    UserDailyMission userDailyMission = getUserDailyMission(userDailyMissionId);
    DailyMissionMaster dailyMissionMaster = userDailyMission.getDailyMissionMaster();
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

//  // 새로운 메서드 - 완료된 퀴즈 결과 조회 (정답 정보 포함)
//  public CompletedQuizResponseDTO getCompletedQuizResult(Long userDailyMissionId) {
//    UserQuizMission userDailyMission = (UserQuizMission) getUserDailyMission(userDailyMissionId);
//
//    // 답안을 제출하지 않은 미션은 결과 조회 불가
//    if (userDailyMission.getSelectedOptionId() == null) {
//      throw new RuntimeException("답안을 제출하지 않은 미션의 결과는 조회할 수 없습니다.");
//    }
//
//    DailyMissionMaster dailyMissionMaster = userDailyMission.getDailyMissionMaster();
//    Quiz quiz = getQuizByMissionId(dailyMissionMaster.getId());
//    List<QuizOptions> quizOptions = quizOptionsRepository.findAllByQuizId(quiz.getId());
//
//    // 사용자가 선택한 답안 정보
//    Long userSelectedOptionId = userDailyMission.getSelectedOptionId();
//
//    // 정답 여부 계산 (엔티티에 저장되지 않으므로 다시 계산)
//    boolean isCorrect =
//        quizOptions.stream()
//            .anyMatch(option -> option.getId().equals(userSelectedOptionId) && option.isCorrect());
//
//    // 사용자가 선택한 답안 번호 계산 (엔티티에 저장되지 않으므로 역계산)
//    Integer userSelectedAnswerNumber = null;
//    for (QuizOptions option : quizOptions) {
//      if (option.getId().equals(userSelectedOptionId)) {
//        userSelectedAnswerNumber = option.getOptionOrder();
//        break;
//      }
//    }
//
//    // 정답 정보 포함하여 DTO 생성
//    List<CompletedQuizResponseDTO.CompletedQuizOptionResponseDTO> optionDTOs =
//        quizOptions.stream()
//            .map(
//                option ->
//                    CompletedQuizResponseDTO.CompletedQuizOptionResponseDTO.builder()
//                        .id(option.getId())
//                        .text(option.getOptionText())
//                        .isAnswer(option.isCorrect()) // 정답 여부
//                        .isSelected(option.getId().equals(userSelectedOptionId))
//                        .optionOrder(option.getOptionOrder())
//                        .build())
//            .collect(Collectors.toList());
//
//    return CompletedQuizResponseDTO.builder()
//        .quizType(quiz.getQuizType())
//        .quizQuestion(quiz.getQuizQuestion())
//        .quizOptions(optionDTOs)
//        .missionId(dailyMissionMaster.getId())
//        .isCorrect(isCorrect)
//        .selectedOptionId(userSelectedOptionId)
//        .selectedAnswerNumber(userSelectedAnswerNumber)
//        .build();
//  }

  // 퀴즈 제출 API
  public CompletedQuizResponseDTO summitQuizAnswer(QuizRequestDTO request, Long userDailyMissionId) {
    UserDailyMission userDailyMission = userDailyMissionRepository.findById(userDailyMissionId).orElseThrow(
            () -> new RuntimeException("미션을 찾을 수 없습니다.")
    );
    DailyMissionMaster dailyMissionMaster = userDailyMission.getDailyMissionMaster();
    Quiz quiz = getQuizByMissionId(dailyMissionMaster.getId());
    List<QuizOptions> quizOptions = quizOptionsRepository.findAllByQuizId(quiz.getId());
    //선택한 선지 정보 불러오기
    QuizOptions selectedQuizOption = quizOptionsRepository.findById(request.getSelectedOptionId()).orElseThrow(
            ()-> new RuntimeException("해당하는 선지를 찾을 수 없습니다.")
    );
    //퀴즈 정답 확인
    boolean isCorrect = quiz.getAnswerNumber().equals(request.getSelectedOptionId());

    // 정답 정보 포함하여 DTO 생성
    List<CompletedQuizResponseDTO.CompletedQuizOptionResponseDTO> optionDTOs =
            quizOptions.stream()
                    .map(
                            option ->
                                    CompletedQuizResponseDTO.CompletedQuizOptionResponseDTO.builder()
                                            .id(option.getId())
                                            .text(option.getOptionText())
                                            .isAnswer(option.getOptionOrder()==quiz.getAnswerNumber()) // 정답 여부
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

  public QuizResponseDTO getQuizByType(QuizType quizType) {
    Quiz quiz = quizRepository.findAllByQuizType(quizType).get(0);
    if (quiz == null) throw new QuizNotFoundException("해당 종류의 퀴즈가 존재하지 않습니다.");
    List<QuizOptions> quizOptions = quizOptionsRepository.findAllByQuizId(quiz.getId());

    // 정답 정보 제외하고 DTO 생성
    List<QuizResponseDTO.QuizOptionResponseDTO> optionDTOs =
        quizOptions.stream()
            .map(
                option ->
                    QuizResponseDTO.QuizOptionResponseDTO.builder()
                        .id(option.getId())
                        .text(option.getOptionText())
                        // isAnswer 필드 제거됨
                        .build())
            .collect(Collectors.toList());

    return QuizResponseDTO.builder()
        .quizType(quiz.getQuizType())
        .quizQuestion(quiz.getQuizQuestion())
        .quizOptions(optionDTOs)
        .quizId(quiz.getId())
        .build();
  }

  // 공통 메서드들
  private UserDailyMission getUserDailyMission(Long userDailyMissionId) {
    return userDailyMissionRepository
        .findById(userDailyMissionId)
        .orElseThrow(() -> new RuntimeException("해당 ID를 갖는 미션이 존재하지 않습니다."));
  }

  private Quiz getQuizByMissionId(Long missionId) {
    return quizRepository
        .findByDailyMissionMaster_Id(missionId)
        .orElseThrow(() -> new RuntimeException("퀴즈가 존재하지 않습니다."));
  }
}
