package com.example.cp_main_be.domain.member.daily_question.service;

import com.example.cp_main_be.domain.member.daily_question.domain.DailyQuestionAnswer;
import com.example.cp_main_be.domain.member.daily_question.domain.repository.DailyQuestionAnswerRepository;
import com.example.cp_main_be.domain.member.daily_question.dto.DailyQuestionAnswerRequest;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.wishTree.WishTreeService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyQuestionAnswerService {

  private final DailyQuestionAnswerRepository dailyQuestionAnswerRepository;
  private final DailyQuestionService dailyQuestionService;
  private final WishTreeService wishTreeService;

  // [핵심] Long userId 대신 User user 객체를 직접 받도록 변경
  public void saveAnswer(User user, DailyQuestionAnswerRequest requestDto) {
    // 불필요한 유저 조회 로직 삭제
    // User user = userRepository.findById(userId)
    //         .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

    LocalDate today = LocalDate.now();

    // 사용자가 오늘 이미 답변했는지 확인
    if (dailyQuestionAnswerRepository.existsByUserAndAnsweredDate(user, today)) {
      throw new IllegalStateException("User has already answered today's question.");
    }

    DailyQuestionAnswer answer =
        DailyQuestionAnswer.builder()
            .user(user) // 매개변수로 받은 user 객체를 바로 사용
            .question(dailyQuestionService.getQuestionById(requestDto.getQuestionId()))
            .answeredDate(today)
            .build();

    answer.setAnswer(requestDto.getAnswer());

    dailyQuestionAnswerRepository.save(answer);

    wishTreeService.addPointsToWishTree(user.getId(), 15L);
  }

  @Transactional(readOnly = true)
  public boolean hasUserAnsweredToday(User user) {
    // 비로그인 사용자는 항상 답변하지 않은 것으로 간주
    if (user == null) {
      return false;
    }
    LocalDate today = LocalDate.now();
    return dailyQuestionAnswerRepository.existsByUserAndAnsweredDate(user, today);
  }
}
