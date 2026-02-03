package com.example.cp_main_be.domain.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.cp_main_be.domain.member.notification.domain.Notification;
import com.example.cp_main_be.domain.member.notification.domain.NotificationType;
import com.example.cp_main_be.domain.member.notification.domain.repository.DeviceTokenRepository;
import com.example.cp_main_be.domain.member.notification.domain.repository.EmitterRepository;
import com.example.cp_main_be.domain.member.notification.domain.repository.NotificationRepository;
import com.example.cp_main_be.domain.member.notification.service.NotificationService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private DeviceTokenRepository deviceTokenRepository;
  @Mock private UserRepository userRepository;
  @Mock private EmitterRepository emitterRepository;
  @Mock private NotificationRepository notificationRepository;

  @InjectMocks private NotificationService notificationService;

  @DisplayName("알림 설정이 꺼져있으면 알림을 저장하지 않는다")
  @Test
  void send_skips_when_notification_disabled() {
    User receiver = User.builder().id(1L).nickname("user").notificationEnabled(false).build();

    notificationService.send(receiver, null, NotificationType.SUNSHINE, "/garden", null);

    verify(notificationRepository, never()).save(any(Notification.class));
  }

  @DisplayName("알림 설정이 켜져있으면 알림을 저장한다")
  @Test
  void send_saves_when_notification_enabled() {
    User receiver = User.builder().id(1L).nickname("user").notificationEnabled(true).build();

    when(notificationRepository.save(any(Notification.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(emitterRepository.findAllEmitterStartWithByUserId(anyString())).thenReturn(Map.of());
    when(deviceTokenRepository.findByUser(receiver)).thenReturn(Optional.empty());

    notificationService.send(receiver, null, NotificationType.SUNSHINE, "/garden", null);

    verify(notificationRepository).save(any(Notification.class));
  }

  @DisplayName("알림 읽음 처리 - 수신자 본인은 가능")
  @Test
  void readNotification_allows_owner() {
    User receiver = User.builder().id(1L).nickname("user").build();
    Notification notification =
        Notification.builder()
            .receiver(receiver)
            .notificationType(NotificationType.SUNSHINE)
            .content("content")
            .url("/garden")
            .thumbnailUrl(null)
            .isRead(false)
            .build();

    when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

    notificationService.readNotification(1L, 10L);

    Assertions.assertTrue(notification.isRead());
  }

  @DisplayName("알림 읽음 처리 - 수신자 본인이 아니면 실패")
  @Test
  void readNotification_denies_non_owner() {
    User receiver = User.builder().id(1L).nickname("user").build();
    Notification notification =
        Notification.builder()
            .receiver(receiver)
            .notificationType(NotificationType.SUNSHINE)
            .content("content")
            .url("/garden")
            .thumbnailUrl(null)
            .isRead(false)
            .build();

    when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

    Assertions.assertThrows(
        IllegalArgumentException.class, () -> notificationService.readNotification(2L, 10L));
  }
}
