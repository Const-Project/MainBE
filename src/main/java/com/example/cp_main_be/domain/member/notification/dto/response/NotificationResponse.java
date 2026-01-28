package com.example.cp_main_be.domain.member.notification.dto.response;

import com.example.cp_main_be.domain.member.notification.domain.Notification;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {
  private Long id;
  private String content;
  private String url;
  private String thumbnailUrl;
  private boolean isRead;
  private String notificationType;
  private LocalDateTime createdAt;

  public static NotificationResponse from(Notification notification) {
    return NotificationResponse.builder()
        .id(notification.getId())
        .content(notification.getContent())
        .url(notification.getUrl())
        .thumbnailUrl(notification.getThumbnailUrl())
        .isRead(notification.isRead())
        .notificationType(notification.getNotificationType().getType())
        .createdAt(notification.getCreatedAt())
        .build();
  }
}
