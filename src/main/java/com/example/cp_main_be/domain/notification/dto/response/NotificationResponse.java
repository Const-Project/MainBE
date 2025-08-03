package com.example.cp_main_be.domain.notification.dto.response;

import com.example.cp_main_be.domain.notification.domain.Notification;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {
  private Long id;
  private String content;
  private String url;
  private boolean isRead;
  private String notificationType;
  private LocalDateTime createdAt;

  public static NotificationResponse from(Notification notification) {
    return NotificationResponse.builder()
        .id(notification.getId())
        .content(notification.getContent())
        .url(notification.getUrl())
        .isRead(notification.isRead())
        .notificationType(notification.getNotificationType().getType())
        .createdAt(notification.getCreatedAt())
        .build();
  }
}
