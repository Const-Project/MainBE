package com.example.cp_main_be.domain.realquiz.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import com.example.cp_main_be.domain.realquiz.RealQuiz;
import com.example.cp_main_be.domain.realquiz.RealQuizOption;
import com.example.cp_main_be.domain.realquiz.UserQuiz;
import com.example.cp_main_be.domain.realquiz.dto.request.RealQuizAnswerRequestDTO;
import com.example.cp_main_be.domain.realquiz.dto.request.RealQuizCreateRequestDTO;
import com.example.cp_main_be.domain.realquiz.dto.response.RealQuizAnswerResponseDTO;
import com.example.cp_main_be.domain.realquiz.dto.response.RealQuizResponseDTO;
import com.example.cp_main_be.domain.realquiz.dto.response.UserQuizCreateResponseDTO;
import com.example.cp_main_be.domain.realquiz.repository.RealQuizOptionRepository;
import com.example.cp_main_be.domain.realquiz.repository.RealQuizRepostitory;
import com.example.cp_main_be.domain.realquiz.repository.UserQuizRepository;
import com.example.cp_main_be.global.exception.QuizNotFoundException;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RealQuizService {

  private final RealQuizRepostitory realQuizRepostitory;
  private final RealQuizOptionRepository realQuizOptionRepository;
  private final UserRepository userRepository;
  private final UserQuizRepository userQuizRepository;

  public RealQuizResponseDTO createRealQuiz(RealQuizCreateRequestDTO requestDTO) {
    // 퀴즈 생성
    RealQuiz realQuiz =
        RealQuiz.builder()
            .quizQuestion(requestDTO.getQuizQuestion())
            .quizType(requestDTO.getQuizType())
            .answerDescription(requestDTO.getAnswerDescription())
            .answerNumber(requestDTO.getAnswerNumber())
            .build();

    RealQuiz savedQuiz = realQuizRepostitory.save(realQuiz);

    // 퀴즈 선지 생성
    List<RealQuizOption> realQuizOptionList =
        requestDTO.getRealQuizOptionList().stream()
            .map(
                realQuizOptionCreateDTO ->
                    RealQuizOption.builder()
                        .optionOrder(realQuizOptionCreateDTO.getOptionOrder())
                        .optionText(realQuizOptionCreateDTO.getOptionText())
                        .realQuiz(savedQuiz)
                        .build())
            .toList();

    realQuizOptionRepository.saveAll(realQuizOptionList);

    List<RealQuizResponseDTO.RealQuizOptionResponseDTO> result =
        transformToRealQuizOptionResponseDTO(realQuizOptionList);
    return RealQuizResponseDTO.builder()
        .quizId(realQuiz.getId())
        .quizQuestion(realQuiz.getQuizQuestion())
        .quizType(realQuiz.getQuizType())
        .answerNumber(realQuiz.getAnswerNumber())
        .answerDescription(realQuiz.getAnswerDescription())
        .quizOptions(result)
        .build();
  }

  // 유저에게 퀴즈 수동 할당
  public UserQuizCreateResponseDTO createUserQuiz(Long userId, Long realQuizId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다"));
    RealQuiz realQuiz =
        realQuizRepostitory
            .findById(realQuizId)
            .orElseThrow(() -> new RuntimeException("퀴즈를 찾을 수 없습니다"));

    UserQuiz userQuiz = UserQuiz.builder().realQuiz(realQuiz).user(user).isCompleted(false).build();
    UserQuiz savedUserQuiz = userQuizRepository.save(userQuiz);

    return UserQuizCreateResponseDTO.builder()
        .quizId(realQuizId)
        .userId(userId)
        .userName(user.getUsername())
        .build();

    // 매일 정각에 삭제
  }

  public RealQuizResponseDTO getRealQuiz(User user, QuizType quizType) {

    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
    UserQuiz userQuiz =
        userQuizRepository.findAllTodayUserQuizByUser(user, startOfDay, endOfDay).stream()
            .findFirst()
            .orElse(null);

    // 퀴즈가 할당되지 않은 경우
    if (userQuiz == null) {
      List<RealQuiz> realQuizList = realQuizRepostitory.findAllByQuizType(quizType);
      if (realQuizList == null) throw new QuizNotFoundException("불러올 퀴즈가 존재하지 않습니다");
      Random random = new Random();
      int rand = random.nextInt(realQuizList.size());
      RealQuiz realQuiz = realQuizList.get(rand);

      // 할당한다.
      userQuiz = UserQuiz.builder().realQuiz(realQuiz).user(user).isCompleted(false).build();
      userQuizRepository.save(userQuiz);

      return transformToRealQuizResponseDTO(realQuiz);

    }
    // 퀴즈가 할당된 경우
    else {
      RealQuiz realQuiz =
          realQuizRepostitory
              .findById(userQuiz.getRealQuiz().getId())
              .orElseThrow(() -> new QuizNotFoundException("퀴즈가 존재하지 않습니다."));
      return transformToRealQuizResponseDTO(realQuiz);
    }
  }

  public RealQuizAnswerResponseDTO getRealQuizAnswer(
      Long quizId, RealQuizAnswerRequestDTO requestDTO, User user) {
    RealQuiz realQuiz =
        realQuizRepostitory
            .findById(quizId)
            .orElseThrow(() -> new QuizNotFoundException("퀴즈를 찾을 수 없습니다."));

    UserQuiz userQuiz =
        userQuizRepository
            .findByUser(user)
            .orElseThrow(() -> new RuntimeException("할당 된 퀴즈가 없습니다."));
    userQuiz.setIsCompleted(true);

    return RealQuizAnswerResponseDTO.builder()
        .answerDescription(realQuiz.getAnswerDescription())
        .selectedOptionNumber(requestDTO.getSelectedOptionOrder())
        .isCorrect(realQuiz.getAnswerNumber().equals(requestDTO.getSelectedOptionOrder()))
        .isCompleted(true)
        .answerNumber(realQuiz.getAnswerNumber())
        .quizType(realQuiz.getQuizType())
        .quizQuestion(realQuiz.getQuizQuestion())
        .build();
  }

  // RealQuiz를 응답 형식으로 변경
  private RealQuizResponseDTO transformToRealQuizResponseDTO(RealQuiz realQuiz) {
    List<RealQuizOption> quizOptions = realQuizOptionRepository.findAllByRealQuiz(realQuiz);
    List<RealQuizResponseDTO.RealQuizOptionResponseDTO> result =
        transformToRealQuizOptionResponseDTO(quizOptions);
    return RealQuizResponseDTO.builder()
        .quizId(realQuiz.getId())
        .quizType(realQuiz.getQuizType())
        .answerNumber(realQuiz.getAnswerNumber())
        .quizQuestion(realQuiz.getQuizQuestion())
        .answerDescription(realQuiz.getAnswerDescription())
        .quizOptions(result)
        .build();
  }

  private List<RealQuizResponseDTO.RealQuizOptionResponseDTO> transformToRealQuizOptionResponseDTO(
      List<RealQuizOption> quizOptions) {
    return quizOptions.stream()
        .map(
            quizOption ->
                RealQuizResponseDTO.RealQuizOptionResponseDTO.builder()
                    .optionOrder(quizOption.getOptionOrder())
                    .optionText(quizOption.getOptionText())
                    .build())
        .toList();
  }
}
