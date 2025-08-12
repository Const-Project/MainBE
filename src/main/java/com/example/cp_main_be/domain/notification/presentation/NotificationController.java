package com.example.cp_main_be.domain.notification.presentation;

import com.example.cp_main_be.domain.notification.dto.request.NotificationSettingsRequest;
import com.example.cp_main_be.domain.notification.dto.request.NotificationTokenRequest;
import com.example.cp_main_be.domain.notification.dto.response.NotificationResponse;
import com.example.cp_main_be.domain.notification.service.NotificationService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "알림 API", description = "알림 관련 기능을 제공합니다.")
public class NotificationController {

  private final NotificationService notificationService;
  private final UserService userService;

  @Operation(summary = "알림 구독", description = "알림을 구독합니다")
  @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(
      @AuthenticationPrincipal User user,
      @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "")
          String lastEventId) {
    return notificationService.subscribe(user.getId(), lastEventId);
  }

  @Operation(summary = "알림 수신", description = "알림을 수신합니다")
  @GetMapping
  public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        ApiResponse.success(notificationService.getNotifications(user.getId())));
  }

  @Operation(summary = "알림 조회", description = "알림 기록을 읽습니다")
  @PatchMapping("/{id}/read")
  public ResponseEntity<ApiResponse<Void>> readNotification(@PathVariable Long id) {
    notificationService.readNotification(id);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "알림 등록", description = "알림 토큰을 등록합니다")
  @PostMapping("/token")
  public ResponseEntity<ApiResponse<Void>> registerNotificationToken(
      @RequestBody @Valid NotificationTokenRequest request) {
    String userUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = userService.findUserByUuid(UUID.fromString(userUuid));
    notificationService.registerOrUpdateDeviceToken(user.getId(), request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "알림 설정", description = "알림 설정을 변경합니다")
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
