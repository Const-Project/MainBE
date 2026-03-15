package com.example.cp_main_be.domain.tracking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.example.cp_main_be.domain.member.daily_question.domain.repository.DailyQuestionAnswerRepository;
import com.example.cp_main_be.domain.member.log.domain.repository.UserDailyActivityLogRepository;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.tracking.domain.TrackingReportView;
import com.example.cp_main_be.domain.tracking.domain.repository.TrackingReportViewRepository;
import com.example.cp_main_be.domain.tracking.dto.TrackingPromptStatusResponse;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackingServiceTest {

  private static final Long USER_ID = 1L;

  @Mock private UserRepository userRepository;
  @Mock private DailyQuestionAnswerRepository dailyQuestionAnswerRepository;
  @Mock private UserDailyActivityLogRepository userDailyActivityLogRepository;
  @Mock private TrackingReportViewRepository trackingReportViewRepository;

  @Captor private ArgumentCaptor<TrackingReportView> trackingReportViewCaptor;

  private TrackingService trackingService;
  private User user;

  @BeforeEach
  void setUp() {
    trackingService =
        spy(
            new TrackingService(
                userRepository,
                dailyQuestionAnswerRepository,
                userDailyActivityLogRepository,
                trackingReportViewRepository));
    user = User.builder().id(USER_ID).nickname("tester").build();
    given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
  }

  @Test
  @DisplayName("14일 연속 perfect day 이고 아직 확인하지 않았으면 eligible true 를 반환한다")
  void getTrackingPromptStatus_eligibleWhenPerfectDaysSatisfied() {
    LocalDate today = LocalDate.of(2026, 3, 15);
    doReturn(today).when(trackingService).getToday();
    given(
            userDailyActivityLogRepository.countPerfectDays(
                eq(user), eq(LocalDate.of(2026, 3, 2)), eq(today)))
        .willReturn(14L);
    given(trackingReportViewRepository.existsByUserAndCycleKey(user, "2026-03-15"))
        .willReturn(false);

    TrackingPromptStatusResponse response = trackingService.getTrackingPromptStatus(USER_ID);

    assertThat(response.isEligible()).isTrue();
    assertThat(response.isAlreadyViewed()).isFalse();
    assertThat(response.getPerfectDayCount()).isEqualTo(14L);
    assertThat(response.getCycleKey()).isEqualTo("2026-03-15");
    assertThat(response.getWindowStart()).isEqualTo("2026-03-02");
    assertThat(response.getWindowEnd()).isEqualTo("2026-03-15");
  }

  @Test
  @DisplayName("14일 미만이면 eligible false 를 반환한다")
  void getTrackingPromptStatus_ineligibleWhenPerfectDaysNotEnough() {
    LocalDate today = LocalDate.of(2026, 3, 15);
    doReturn(today).when(trackingService).getToday();
    given(userDailyActivityLogRepository.countPerfectDays(any(), any(), any())).willReturn(13L);
    given(trackingReportViewRepository.existsByUserAndCycleKey(user, "2026-03-15"))
        .willReturn(false);

    TrackingPromptStatusResponse response = trackingService.getTrackingPromptStatus(USER_ID);

    assertThat(response.isEligible()).isFalse();
    assertThat(response.isAlreadyViewed()).isFalse();
  }

  @Test
  @DisplayName("같은 cycleKey 를 이미 확인했으면 eligible false 를 반환한다")
  void getTrackingPromptStatus_ineligibleWhenAlreadyViewed() {
    LocalDate today = LocalDate.of(2026, 3, 15);
    doReturn(today).when(trackingService).getToday();
    given(userDailyActivityLogRepository.countPerfectDays(any(), any(), any())).willReturn(14L);
    given(trackingReportViewRepository.existsByUserAndCycleKey(user, "2026-03-15"))
        .willReturn(true);

    TrackingPromptStatusResponse response = trackingService.getTrackingPromptStatus(USER_ID);

    assertThat(response.isEligible()).isFalse();
    assertThat(response.isAlreadyViewed()).isTrue();
  }

  @Test
  @DisplayName("유효한 cycleKey confirm 은 확인 이력을 저장한다")
  void confirmTrackingPrompt_savesViewHistory() {
    LocalDate today = LocalDate.of(2026, 3, 15);
    doReturn(today).when(trackingService).getToday();
    given(trackingReportViewRepository.existsByUserAndCycleKey(user, "2026-03-15"))
        .willReturn(false);

    trackingService.confirmTrackingPrompt(USER_ID, "2026-03-15");

    verify(trackingReportViewRepository).save(trackingReportViewCaptor.capture());
    TrackingReportView saved = trackingReportViewCaptor.getValue();
    assertThat(saved.getUser()).isEqualTo(user);
    assertThat(saved.getCycleKey()).isEqualTo("2026-03-15");
    assertThat(saved.getViewedAt()).isNotNull();
  }

  @Test
  @DisplayName("중복 confirm 요청은 저장 없이 성공 처리한다")
  void confirmTrackingPrompt_isIdempotent() {
    LocalDate today = LocalDate.of(2026, 3, 15);
    doReturn(today).when(trackingService).getToday();
    given(trackingReportViewRepository.existsByUserAndCycleKey(user, "2026-03-15"))
        .willReturn(true);

    trackingService.confirmTrackingPrompt(USER_ID, "2026-03-15");

    verify(trackingReportViewRepository, never()).save(any());
  }

  @Test
  @DisplayName("현재 주기와 다른 cycleKey 로 confirm 하면 INVALID_REQUEST 예외가 발생한다")
  void confirmTrackingPrompt_throwsWhenCycleKeyIsInvalid() {
    LocalDate today = LocalDate.of(2026, 3, 15);
    doReturn(today).when(trackingService).getToday();

    assertThatThrownBy(() -> trackingService.confirmTrackingPrompt(USER_ID, "2026-03-14"))
        .isInstanceOf(CustomApiException.class)
        .extracting(ex -> ((CustomApiException) ex).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_REQUEST);
  }

  @Test
  @DisplayName("다음 주기에 새 cycleKey 가 열리면 다시 eligible true 가 된다")
  void getTrackingPromptStatus_eligibleAgainOnNextCycle() {
    doReturn(LocalDate.of(2026, 3, 15), LocalDate.of(2026, 3, 16)).when(trackingService).getToday();
    given(userDailyActivityLogRepository.countPerfectDays(any(), any(), any())).willReturn(14L);
    given(trackingReportViewRepository.existsByUserAndCycleKey(user, "2026-03-15"))
        .willReturn(true);
    given(trackingReportViewRepository.existsByUserAndCycleKey(user, "2026-03-16"))
        .willReturn(false);

    TrackingPromptStatusResponse firstResponse = trackingService.getTrackingPromptStatus(USER_ID);
    TrackingPromptStatusResponse secondResponse = trackingService.getTrackingPromptStatus(USER_ID);

    assertThat(firstResponse.isEligible()).isFalse();
    assertThat(firstResponse.getCycleKey()).isEqualTo("2026-03-15");
    assertThat(secondResponse.isEligible()).isTrue();
    assertThat(secondResponse.getCycleKey()).isEqualTo("2026-03-16");
  }
}
