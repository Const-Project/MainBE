package com.example.cp_main_be.domain.member.notification.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationSettingsRequest {
  private boolean notificationEnabled; // 전체 알림 수신 여부
  private boolean marketingConsent; // [추가] 마케팅 수신 동의 여부
}
