package com.example.cp_main_be.domain.tracking.service;

import com.example.cp_main_be.domain.member.daily_question.domain.AnswerType;
import com.example.cp_main_be.domain.member.daily_question.domain.DailyQuestionAnswer;
import com.example.cp_main_be.domain.member.daily_question.domain.repository.DailyQuestionAnswerRepository;
import com.example.cp_main_be.domain.member.log.domain.repository.UserDailyActivityLogRepository;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.tracking.dto.TrackingReportResponse;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackingService {

  private final UserRepository userRepository;
  private final DailyQuestionAnswerRepository dailyQuestionAnswerRepository;
  private final UserDailyActivityLogRepository userDailyActivityLogRepository;
  private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

  public TrackingReportResponse getTrackingReport(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

    LocalDate today = LocalDate.now(KOREA_ZONE);
    LocalDate twoWeeksAgo = today.minusDays(13); // 오늘 포함 14일

    // 1. 설문 답변 데이터 가져오기
    List<DailyQuestionAnswer> answers =
        dailyQuestionAnswerRepository.findAllByUserAndAnsweredDateBetween(user, twoWeeksAgo, today);

    // 2. 점수 계산 및 분류
    String trackingType = analyzeTrackingType(answers, twoWeeksAgo);

    // 3. 칭찬 카운트 (완벽한 날)
    long praiseDayCount = userDailyActivityLogRepository.countPerfectDays(user, twoWeeksAgo, today);

    int totalScore = answers.stream().mapToInt(this::getScore).sum();

    // 4. 메시지 선정
    String message = getMessageByType(trackingType);

    return TrackingReportResponse.builder()
        .trackingType(trackingType)
        .totalScore(totalScore)
        .praiseDayCount(praiseDayCount)
        .message(message)
        .build();
  }

  private String getMessageByType(String trackingType) {
    if ("ALWAYS_GOOD".equals(trackingType)) {
      return "항상 긍정적인 마음을 유지하고 계시네요! 정말 멋져요. 🌿";
    }
    if ("IMPROVING".equals(trackingType)) {
      return "마음이 점점 더 단단해지고 있어요! 긍정적인 변화가 보입니다. 🌱";
    }
    // NEEDS_COMFORT
    return getRandomComfortMessage();
  }

  private String getRandomComfortMessage() {
    List<String> comfortMessages =
        List.of(
            "힘든 순간은 결국 지나가기 마련이에요. 잠시 쉬어가도 괜찮아요. ☁️",
            "당신은 충분히 잘하고 있어요. 스스로를 믿어주세요. 💙",
            "오늘 하루도 수고 많았어요. 따뜻한 차 한 잔 어때요? ☕",
            "자책하지 마세요. 당신의 속도대로 가도 괜찮습니다. 🐢",
            "비 온 뒤에 땅이 굳어지듯, 지금의 시련이 당신을 더 단단하게 만들 거예요. 💪");
    int index = (int) (Math.random() * comfortMessages.size());
    return comfortMessages.get(index);
  }

  private String analyzeTrackingType(List<DailyQuestionAnswer> answers, LocalDate startDate) {
    int totalScore = 0;
    int firstWeekScore = 0;
    int firstWeekCount = 0;
    int secondWeekScore = 0;
    int secondWeekCount = 0;

    LocalDate weekBoundary = startDate.plusDays(7);

    for (DailyQuestionAnswer answer : answers) {
      int score = getScore(answer);
      totalScore += score;

      if (answer.getAnsweredDate().isBefore(weekBoundary)) {
        firstWeekScore += score;
        firstWeekCount++;
      } else {
        secondWeekScore += score;
        secondWeekCount++;
      }
    }

    // Type 1: 항상 좋은 (20점 이상) - 답변이 너무 적으면 제외할 수도 있지만, 일단 단순 합산 기준
    if (totalScore >= 20) {
      return "ALWAYS_GOOD";
    }

    // Type 2: 점점 좋아짐
    double firstWeekAvg = firstWeekCount > 0 ? (double) firstWeekScore / firstWeekCount : 0;
    double secondWeekAvg = secondWeekCount > 0 ? (double) secondWeekScore / secondWeekCount : 0;

    // 1주차보다 2주차가 평균 0.5점 이상 높으면 좋아진 것으로 간주
    if (secondWeekAvg > firstWeekAvg + 0.5) {
      return "IMPROVING";
    }

    // Type 3: 위로가 필요함 (기본값)
    return "NEEDS_COMFORT";
  }

  private int getScore(DailyQuestionAnswer answer) {
    if (answer.getAnswer() == AnswerType.YES) return 2;
    if (answer.getAnswer() == AnswerType.NEUTRAL) return 1;
    return 0; // NO
  }
}
