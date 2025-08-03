package com.example.cp_main_be.domain.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.cp_main_be.domain.notification.domain.DeviceToken;
import com.example.cp_main_be.domain.notification.domain.repository.DeviceTokenRepository;
import com.example.cp_main_be.domain.notification.dto.request.NotificationSettingsRequest;
import com.example.cp_main_be.domain.notification.dto.request.NotificationTokenRequest;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.exception.UserNotFoundException;
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

  @InjectMocks private NotificationService notificationService;

  @DisplayName("디바이스 토큰 등록 또는 갱신 성공 - 새로운 토큰")
  @Test
  void registerOrUpdateDeviceToken_success_new() {
    // given
    Long userId = 1L;
    String token = "newToken";
    NotificationTokenRequest request = new NotificationTokenRequest();
    request.setToken(token);

    User user = User.builder().id(userId).username("testuser").build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(deviceTokenRepository.findByUser(user)).willReturn(Optional.empty());
    given(deviceTokenRepository.save(any(DeviceToken.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // when
    notificationService.registerOrUpdateDeviceToken(userId, request);

    // then
    verify(deviceTokenRepository).save(any(DeviceToken.class));
  }

  @DisplayName("디바이스 토큰 등록 또는 갱신 성공 - 기존 토큰 갱신")
  @Test
  void registerOrUpdateDeviceToken_success_update() {
    // given
    Long userId = 1L;
    String oldToken = "oldToken";
    String newToken = "newToken";
    NotificationTokenRequest request = new NotificationTokenRequest();
    request.setToken(newToken);

    User user = User.builder().id(userId).username("testuser").build();
    DeviceToken existingToken = DeviceToken.builder().user(user).token(oldToken).build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(deviceTokenRepository.findByUser(user)).willReturn(Optional.of(existingToken));
    given(deviceTokenRepository.save(any(DeviceToken.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // when
    notificationService.registerOrUpdateDeviceToken(userId, request);

    // then
    Assertions.assertEquals(newToken, existingToken.getToken());
    verify(deviceTokenRepository).save(any(DeviceToken.class));
  }

  @DisplayName("디바이스 토큰 등록 또는 갱신 실패 - 사용자 없음")
  @Test
  void registerOrUpdateDeviceToken_fail_userNotFound() {
    // given
    Long userId = 1L;
    NotificationTokenRequest request = new NotificationTokenRequest();
    request.setToken("token");

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    Assertions.assertThrows(
        UserNotFoundException.class,
        () -> notificationService.registerOrUpdateDeviceToken(userId, request));
    verify(deviceTokenRepository, org.mockito.Mockito.never()).save(any(DeviceToken.class));
  }

  @DisplayName("알림 설정 업데이트 성공")
  @Test
  void updateNotificationSettings_success() {
    // given
    Long userId = 1L;
    NotificationSettingsRequest request = new NotificationSettingsRequest();
    request.setNotificationEnabled(true);

    User user = User.builder().id(userId).username("testuser").build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    // TODO: User 엔티티에 notificationEnabled 필드 추가 후 테스트 로직 보완

    // when
    notificationService.updateNotificationSettings(userId, request);

    // then
    // verify(userRepository).save(user); // User 엔티티에 필드 추가 후 주석 해제
  }

  @DisplayName("알림 설정 업데이트 실패 - 사용자 없음")
  @Test
  void updateNotificationSettings_fail_userNotFound() {
    // given
    Long userId = 1L;
    NotificationSettingsRequest request = new NotificationSettingsRequest();
    request.setNotificationEnabled(true);

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    Assertions.assertThrows(
        UserNotFoundException.class,
        () -> notificationService.updateNotificationSettings(userId, request));
    // verify(userRepository, org.mockito.Mockito.never()).save(any(User.class));
  }
}
