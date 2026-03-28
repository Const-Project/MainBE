package com.example.cp_main_be.domain.member.notification.presentation;

import com.example.cp_main_be.domain.member.notification.dto.request.NotificationSettingsRequest;
import com.example.cp_main_be.domain.member.notification.dto.request.NotificationTokenRequest;
import com.example.cp_main_be.domain.member.notification.dto.response.NotificationResponse;
import com.example.cp_main_be.domain.member.notification.service.NotificationService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

  @Operation(summary = "알림 읽음 처리", description = "내 알림을 읽음 처리합니다")
  @PatchMapping("/{id}/read")
  public ResponseEntity<ApiResponse<Void>> readNotification(
      @AuthenticationPrincipal User user, @PathVariable Long id) {
    notificationService.readNotification(user.getId(), id);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "알림 등록", description = "알림 토큰을 등록합니다")
  @PostMapping("/token")
  public ResponseEntity<ApiResponse<Void>> registerNotificationToken(
      @AuthenticationPrincipal User user, @RequestBody @Valid NotificationTokenRequest request) {
    notificationService.registerOrUpdateDeviceToken(user.getId(), request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "알림 토큰 해제", description = "현재 사용자의 알림 토큰을 해제합니다")
  @DeleteMapping("/token")
  public ResponseEntity<ApiResponse<Void>> deleteNotificationToken(
      @AuthenticationPrincipal User user) {
    notificationService.deleteDeviceToken(user.getId());
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "알림/마케팅 설정 조회", description = "현재 알림 및 마케팅 수신 동의 설정을 조회합니다")
  @GetMapping("/settings")
  public ResponseEntity<
          ApiResponse<
              com.example.cp_main_be.domain.member.notification.dto.response
                  .NotificationSettingsResponse>>
      getNotificationSettings(@AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        ApiResponse.success(notificationService.getNotificationSettings(user.getId())));
  }

  @Operation(summary = "알림/마케팅 설정 변경", description = "알림 및 마케팅 수신 동의 설정을 변경합니다")
  @PatchMapping("/settings")
  public ResponseEntity<ApiResponse<Void>> updateNotificationSettings(
      @AuthenticationPrincipal User user, @RequestBody @Valid NotificationSettingsRequest request) {
    notificationService.updateNotificationSettings(user.getId(), request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
