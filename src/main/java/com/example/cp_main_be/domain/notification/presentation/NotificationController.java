package com.example.cp_main_be.domain.notification.presentation;

import com.example.cp_main_be.domain.notification.dto.request.NotificationSettingsRequest;
import com.example.cp_main_be.domain.notification.dto.request.NotificationTokenRequest;
import com.example.cp_main_be.domain.notification.dto.response.NotificationResponse;
import com.example.cp_main_be.domain.notification.service.NotificationService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

  private final NotificationService notificationService;
  private final UserService userService;

  @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(
      @AuthenticationPrincipal User user,
      @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "")
          String lastEventId) {
    return notificationService.subscribe(user.getId(), lastEventId);
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        ApiResponse.success(notificationService.getNotifications(user.getId())));
  }

  @PatchMapping("/{id}/read")
  public ResponseEntity<ApiResponse<Void>> readNotification(@PathVariable Long id) {
    notificationService.readNotification(id);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PostMapping("/token")
  public ResponseEntity<ApiResponse<Void>> registerNotificationToken(
      @RequestBody @Valid NotificationTokenRequest request) {
    String userUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = userService.findUserByUuid(UUID.fromString(userUuid));
    notificationService.registerOrUpdateDeviceToken(user.getId(), request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PatchMapping("/settings")
  public ResponseEntity<ApiResponse<Void>> updateNotificationSettings(
      @RequestBody @Valid NotificationSettingsRequest request) {
    String userUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = userService.findUserByUuid(UUID.fromString(userUuid));
    notificationService.updateNotificationSettings(user.getId(), request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
