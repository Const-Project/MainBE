package com.example.cp_main_be.domain.realquiz.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import com.example.cp_main_be.domain.mission.wishTree.WishTreeService;
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
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RealQuizService {
  private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

  private final RealQuizRepostitory realQuizRepostitory;
  private final RealQuizOptionRepository realQuizOptionRepository;
  private final UserRepository userRepository;
  private final UserQuizRepository userQuizRepository;
  private final WishTreeService wishTreeService;

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
    LocalDate today = LocalDate.now(KOREA_ZONE);
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(23, 59, 59);
    /*
     * 한글 주석:
     * 실시간 퀴즈는 하루에 한 문제만 유지해야 하므로
     * 타입과 무관하게 오늘 가장 마지막으로 할당된 퀴즈를 재사용한다.
     */
    UserQuiz userQuiz =
        userQuizRepository
            .findTopByUserAndCreatedAtBetweenOrderByCreatedAtDesc(user, startOfDay, endOfDay)
            .orElse(null);

    // 퀴즈가 할당되지 않은 경우
    if (userQuiz == null) {
      List<RealQuiz> realQuizList = realQuizRepostitory.findAllByQuizType(quizType);
      if (realQuizList == null || realQuizList.isEmpty()) {
        throw new QuizNotFoundException("불러올 퀴즈가 존재하지 않습니다");
      }
      Random random = new Random();
      int rand = random.nextInt(realQuizList.size());
      RealQuiz realQuiz = realQuizList.get(rand);

      // 할당한다.
      userQuiz = UserQuiz.builder().realQuiz(realQuiz).user(user).isCompleted(false).build();
      userQuizRepository.save(userQuiz);

      return transformToRealQuizResponseDTO(realQuiz, userQuiz);

    }
    // 퀴즈가 할당된 경우
    else {
      RealQuiz realQuiz =
          realQuizRepostitory
              .findById(userQuiz.getRealQuiz().getId())
              .orElseThrow(() -> new QuizNotFoundException("퀴즈가 존재하지 않습니다."));
      return transformToRealQuizResponseDTO(realQuiz, userQuiz);
    }
  }

  public RealQuizAnswerResponseDTO getRealQuizAnswer(
      Long quizId, RealQuizAnswerRequestDTO requestDTO, User user) {
    LocalDate today = LocalDate.now(KOREA_ZONE);
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(23, 59, 59);

    RealQuiz realQuiz =
        realQuizRepostitory
            .findById(quizId)
            .orElseThrow(() -> new QuizNotFoundException("퀴즈를 찾을 수 없습니다."));

    UserQuiz userQuiz =
        userQuizRepository
            .findTopByUserAndRealQuizAndCreatedAtBetweenOrderByCreatedAtDesc(
                user, realQuiz, startOfDay, endOfDay)
            .orElseThrow(() -> new RuntimeException("오늘 할당 된 퀴즈가 없습니다."));

    /*
     * 한글 주석:
     * 이미 제출한 퀴즈는 같은 결과를 그대로 반환해
     * 재진입/중복 요청 시 보상과 상태가 다시 반영되지 않게 막는다.
     */
    if (Boolean.TRUE.equals(userQuiz.getIsCompleted())
        && userQuiz.getSelectedOptionOrder() != null) {
      return buildAnswerResponse(realQuiz, userQuiz.getSelectedOptionOrder());
    }

    userQuiz.setIsCompleted(true);
    userQuiz.setSelectedOptionOrder(requestDTO.getSelectedOptionOrder());

    boolean isCorrect = realQuiz.getAnswerNumber().equals(requestDTO.getSelectedOptionOrder());

    if (isCorrect) {
      wishTreeService.addPointsToWishTree(user.getId(), 15L);
    }

    return buildAnswerResponse(realQuiz, requestDTO.getSelectedOptionOrder());
  }

  // RealQuiz를 응답 형식으로 변경
  private RealQuizResponseDTO transformToRealQuizResponseDTO(RealQuiz realQuiz, UserQuiz userQuiz) {
    List<RealQuizOption> quizOptions = realQuizOptionRepository.findAllByRealQuiz(realQuiz);
    List<RealQuizResponseDTO.RealQuizOptionResponseDTO> result =
        transformToRealQuizOptionResponseDTO(quizOptions);
    /*
     * 한글 주석:
     * 퀴즈 조회 응답 자체에 완료 여부, 선택한 선지, 해설을 같이 담아야
     * 앱이 화면 재진입 후에도 이전 풀이 상태를 그대로 복원할 수 있다.
     */
    boolean isCompleted =
        userQuiz != null
            && Boolean.TRUE.equals(userQuiz.getIsCompleted())
            && userQuiz.getSelectedOptionOrder() != null;
    Integer selectedOptionNumber = isCompleted ? userQuiz.getSelectedOptionOrder() : null;

    return RealQuizResponseDTO.builder()
        .quizId(realQuiz.getId())
        .quizType(realQuiz.getQuizType())
        .quizQuestion(realQuiz.getQuizQuestion())
        .isCompleted(isCompleted)
        .selectedOptionNumber(selectedOptionNumber)
        .answerNumber(isCompleted ? realQuiz.getAnswerNumber() : null)
        .isCorrect(isCompleted ? realQuiz.getAnswerNumber().equals(selectedOptionNumber) : null)
        .answerDescription(isCompleted ? realQuiz.getAnswerDescription() : null)
        .quizOptions(result)
        .build();
  }

  private RealQuizAnswerResponseDTO buildAnswerResponse(
      RealQuiz realQuiz, Integer selectedOptionOrder) {
    return RealQuizAnswerResponseDTO.builder()
        .answerDescription(realQuiz.getAnswerDescription())
        .selectedOptionNumber(selectedOptionOrder)
        .isCorrect(realQuiz.getAnswerNumber().equals(selectedOptionOrder))
        .isCompleted(true)
        .answerNumber(realQuiz.getAnswerNumber())
        .quizType(realQuiz.getQuizType())
        .quizQuestion(realQuiz.getQuizQuestion())
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
