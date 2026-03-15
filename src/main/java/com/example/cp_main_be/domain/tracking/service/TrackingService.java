package com.example.cp_main_be.domain.tracking.service;

import com.example.cp_main_be.domain.member.daily_question.domain.AnswerType;
import com.example.cp_main_be.domain.member.daily_question.domain.DailyQuestionAnswer;
import com.example.cp_main_be.domain.member.daily_question.domain.repository.DailyQuestionAnswerRepository;
import com.example.cp_main_be.domain.member.log.domain.repository.UserDailyActivityLogRepository;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.tracking.domain.TrackingReportView;
import com.example.cp_main_be.domain.tracking.domain.repository.TrackingReportViewRepository;
import com.example.cp_main_be.domain.tracking.dto.TrackingPromptStatusResponse;
import com.example.cp_main_be.domain.tracking.dto.TrackingReportResponse;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackingService {

  private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
  private static final int TRACKING_WINDOW_DAYS = 14;
  private static final long REQUIRED_PERFECT_DAY_COUNT = 14L;
  private static final String TRACKING_PROMPT_MESSAGE = "2주 동안 꾸준히 돌봐주셨어요.";

  private final UserRepository userRepository;
  private final DailyQuestionAnswerRepository dailyQuestionAnswerRepository;
  private final UserDailyActivityLogRepository userDailyActivityLogRepository;
  private final TrackingReportViewRepository trackingReportViewRepository;

  public TrackingReportResponse getTrackingReport(Long userId) {
    User user = getUser(userId);
    TrackingWindow window = resolveCurrentWindow();

    // 한글 주석:
    // 기존 2주 리포트 조회는 유지하되, 윈도우 계산과 perfect day 계산은 공통 로직을 재사용한다.
    List<DailyQuestionAnswer> answers =
        dailyQuestionAnswerRepository.findAllByUserAndAnsweredDateBetween(
            user, window.getWindowStart(), window.getWindowEnd());
    String trackingType = analyzeTrackingType(answers, window.getWindowStart());
    long praiseDayCount =
        calculatePerfectDayCount(user, window.getWindowStart(), window.getWindowEnd());
    int totalScore = answers.stream().mapToInt(this::getScore).sum();
    String message = getMessageByType(trackingType);

    return TrackingReportResponse.builder()
        .trackingType(trackingType)
        .totalScore(totalScore)
        .praiseDayCount(praiseDayCount)
        .message(message)
        .build();
  }

  public TrackingPromptStatusResponse getTrackingPromptStatus(Long userId) {
    User user = getUser(userId);
    TrackingWindow window = resolveCurrentWindow();

    // 한글 주석:
    // 홈 자동 노출 여부는 앱이 계산하지 않고 서버가 현재 14일 주기의 상태를 최종 판정한다.
    long perfectDayCount =
        calculatePerfectDayCount(user, window.getWindowStart(), window.getWindowEnd());
    boolean alreadyViewed =
        trackingReportViewRepository.existsByUserAndCycleKey(user, window.getCycleKey());
    boolean eligible = perfectDayCount >= REQUIRED_PERFECT_DAY_COUNT && !alreadyViewed;

    return TrackingPromptStatusResponse.builder()
        .eligible(eligible)
        .alreadyViewed(alreadyViewed)
        .perfectDayCount(perfectDayCount)
        .cycleKey(window.getCycleKey())
        .windowStart(window.getWindowStart().toString())
        .windowEnd(window.getWindowEnd().toString())
        .message(TRACKING_PROMPT_MESSAGE)
        .build();
  }

  @Transactional
  public void confirmTrackingPrompt(Long userId, String cycleKey) {
    User user = getUser(userId);
    TrackingWindow window = resolveCurrentWindow();

    // 한글 주석:
    // confirm 은 현재 주기와 동일한 cycleKey 만 허용하고, 이미 저장된 경우에는 그대로 성공 처리한다.
    if (!StringUtils.hasText(cycleKey) || !window.getCycleKey().equals(cycleKey)) {
      throw new CustomApiException(ErrorCode.INVALID_REQUEST, "현재 주기와 일치하지 않는 cycleKey 입니다.");
    }

    if (trackingReportViewRepository.existsByUserAndCycleKey(user, cycleKey)) {
      return;
    }

    trackingReportViewRepository.save(
        TrackingReportView.builder()
            .user(user)
            .cycleKey(cycleKey)
            .viewedAt(LocalDateTime.now(KOREA_ZONE))
            .build());
  }

  public long calculatePerfectDayCount(Long userId, LocalDate windowStart, LocalDate windowEnd) {
    User user = getUser(userId);
    return calculatePerfectDayCount(user, windowStart, windowEnd);
  }

  public String resolveCycleKey(LocalDate windowStart, LocalDate windowEnd) {
    return windowEnd.toString();
  }

  private long calculatePerfectDayCount(User user, LocalDate windowStart, LocalDate windowEnd) {
    return userDailyActivityLogRepository.countPerfectDays(user, windowStart, windowEnd);
  }

  private TrackingWindow resolveCurrentWindow() {
    LocalDate today = LocalDate.now(KOREA_ZONE);
    LocalDate windowStart = today.minusDays(TRACKING_WINDOW_DAYS - 1L);
    return TrackingWindow.builder()
        .windowStart(windowStart)
        .windowEnd(today)
        .cycleKey(resolveCycleKey(windowStart, today))
        .build();
  }

  private User getUser(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
  }

  private String getMessageByType(String trackingType) {
    if ("ALWAYS_GOOD".equals(trackingType)) {
      return "항상 긍정적인 마음을 유지하고 계시네요! 정말 멋져요. 🌿";
    }
    if ("IMPROVING".equals(trackingType)) {
      return "마음이 점점 더 단단해지고 있어요! 긍정적인 변화가 보입니다. 🌱";
    }
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

    if (totalScore >= 20) {
      return "ALWAYS_GOOD";
    }

    double firstWeekAvg = firstWeekCount > 0 ? (double) firstWeekScore / firstWeekCount : 0;
    double secondWeekAvg = secondWeekCount > 0 ? (double) secondWeekScore / secondWeekCount : 0;

    if (secondWeekAvg > firstWeekAvg + 0.5) {
      return "IMPROVING";
    }

    return "NEEDS_COMFORT";
  }

  private int getScore(DailyQuestionAnswer answer) {
    if (answer.getAnswer() == AnswerType.YES) {
      return 2;
    }
    if (answer.getAnswer() == AnswerType.NEUTRAL) {
      return 1;
    }
    return 0;
  }

  @Getter
  @Builder
  private static class TrackingWindow {
    private LocalDate windowStart;
    private LocalDate windowEnd;
    private String cycleKey;
  }
}
