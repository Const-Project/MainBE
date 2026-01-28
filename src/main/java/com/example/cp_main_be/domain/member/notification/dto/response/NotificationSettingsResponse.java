package com.example.cp_main_be.domain.member.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationSettingsResponse {
  private boolean notificationEnabled;
  private boolean marketingConsent;
}
