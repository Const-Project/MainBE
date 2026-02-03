package com.example.cp_main_be.global.listener;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.cp_main_be.domain.garden.garden.service.GardenService;
import com.example.cp_main_be.domain.member.notification.domain.NotificationType;
import com.example.cp_main_be.domain.member.notification.service.NotificationService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.mission.wishTree.WishTreeService;
import com.example.cp_main_be.domain.reports.domain.Reports;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.global.event.ReportProcessedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

  @Mock private NotificationService notificationService;
  @Mock private DiaryRepository diaryRepository;
  @Mock private AvatarPostRepository avatarPostRepository;
  @Mock private WishTreeService wishTreeService;
  @Mock private GardenService gardenService;

  @InjectMocks private NotificationEventListener listener;

  @DisplayName("신고 처리 이벤트 발생 시 신고자에게 알림 전송")
  @Test
  void handleReportProcessedEvent_sendsNotification() {
    User reporter = User.builder().id(10L).nickname("reporter").build();
    Reports report = Reports.builder().id(1L).user(reporter).build();

    listener.handleReportProcessedEvent(new ReportProcessedEvent(report));

    verify(notificationService)
        .send(reporter, null, NotificationType.REPORT_PROCESSED, "/reports/1", null);
  }

  @DisplayName("신고자 정보가 없으면 알림을 전송하지 않음")
  @Test
  void handleReportProcessedEvent_skipsWhenReporterMissing() {
    Reports report = Reports.builder().id(2L).user(null).build();

    listener.handleReportProcessedEvent(new ReportProcessedEvent(report));

    verify(notificationService, never())
        .send(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
  }
}
