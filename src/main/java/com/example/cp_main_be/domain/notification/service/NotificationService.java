package com.example.cp_main_be.domain.notification.service;

import com.example.cp_main_be.domain.notification.domain.DeviceToken;
import com.example.cp_main_be.domain.notification.domain.repository.DeviceTokenRepository;
import com.example.cp_main_be.domain.notification.dto.request.NotificationSettingsRequest;
import com.example.cp_main_be.domain.notification.dto.request.NotificationTokenRequest;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

  private final DeviceTokenRepository deviceTokenRepository;
  private final UserRepository userRepository;

  public void registerOrUpdateDeviceToken(Long userId, NotificationTokenRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

    DeviceToken deviceToken =
        deviceTokenRepository.findByUser(user).orElse(DeviceToken.builder().user(user).build());

    deviceToken.setToken(request.getToken()); // TODO: DeviceToken 엔티티에 setToken 메서드 추가 필요
    deviceTokenRepository.save(deviceToken);
  }

  public void updateNotificationSettings(Long userId, NotificationSettingsRequest request) {
    // TODO: 알림 설정 저장 로직 구현 (User 엔티티에 알림 설정 필드 추가 필요)
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
    // user.setNotificationEnabled(request.isNotificationEnabled());
    // userRepository.save(user);
  }
}
