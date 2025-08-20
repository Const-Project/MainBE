package com.example.cp_main_be.domain.member.notification.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationSettingsRequest {
  private boolean notificationEnabled; // 전체 알림 수신 여부
  // TODO: 활동별 알림 수신 여부 필드 추가 (예: boolean commentNotificationEnabled;)
}
