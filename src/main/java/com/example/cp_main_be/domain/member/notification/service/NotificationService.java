package com.example.cp_main_be.domain.member.notification.service;

import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenRepository;
import com.example.cp_main_be.domain.member.notification.domain.DeviceToken;
import com.example.cp_main_be.domain.member.notification.domain.Notification;
import com.example.cp_main_be.domain.member.notification.domain.NotificationType;
import com.example.cp_main_be.domain.member.notification.domain.repository.DeviceTokenRepository;
import com.example.cp_main_be.domain.member.notification.domain.repository.EmitterRepository;
import com.example.cp_main_be.domain.member.notification.domain.repository.NotificationRepository;
import com.example.cp_main_be.domain.member.notification.dto.request.NotificationSettingsRequest;
import com.example.cp_main_be.domain.member.notification.dto.request.NotificationTokenRequest;
import com.example.cp_main_be.domain.member.notification.dto.response.NotificationResponse;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationService {

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

  private final DeviceTokenRepository deviceTokenRepository;
  private final UserRepository userRepository;
  private final GardenRepository gardenRepository;
  private final EmitterRepository emitterRepository;
  private final NotificationRepository notificationRepository;
  private final MeterRegistry meterRegistry;

  @Value("${notification.debug.enabled:false}")
  private boolean notificationDebugEnabled;

  @Value("${notification.debug.sample-rate:1.0}")
  private double notificationDebugSampleRate;

  private final AtomicLong deviceTokenMissingCount = new AtomicLong(0L);
  private final AtomicLong deviceTokenTotalUsers = new AtomicLong(0L);
  private final AtomicLong deviceTokenHasTokenCount = new AtomicLong(0L);
  private final AtomicReference<Double> deviceTokenMissingRatio = new AtomicReference<>(0.0d);

  @PostConstruct
  void registerMetrics() {
    Gauge.builder("notification.device_token.users", deviceTokenMissingCount, AtomicLong::get)
        .tag("status", "missing")
        .tag("window", "7d")
        .register(meterRegistry);
    Gauge.builder("notification.device_token.users", deviceTokenTotalUsers, AtomicLong::get)
        .tag("status", "total")
        .tag("window", "7d")
        .register(meterRegistry);
    Gauge.builder("notification.device_token.users", deviceTokenHasTokenCount, AtomicLong::get)
        .tag("status", "has_token")
        .tag("window", "7d")
        .register(meterRegistry);
    Gauge.builder(
            "notification.device_token.missing.ratio",
            deviceTokenMissingRatio,
            AtomicReference::get)
        .tag("window", "7d")
        .register(meterRegistry);
  }

  public SseEmitter subscribe(Long userId, String lastEventId) {
    String emitterId = makeTimeIncludeId(userId);
    SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));
    emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
    emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

    String eventId = makeTimeIncludeId(userId);
    sendNotification(emitter, eventId, emitterId, "EventStream Created. [userId=" + userId + "]");

    if (hasLostData(lastEventId)) {
      sendLostData(lastEventId, userId, emitterId, emitter);
    }

    debugEvent(
        "SSE_SUBSCRIBED",
        "userId={}, emitterId={}, hasLastEventId={}",
        userId,
        emitterId,
        hasLostData(lastEventId));
    return emitter;
  }

  public void send(
      User receiver,
      User sender,
      NotificationType notificationType,
      String url,
      String thumbnailUrl) {
    if (!Boolean.TRUE.equals(receiver.getNotificationEnabled())) {
      debugEvent(
          "NOTIFICATION_SKIPPED",
          "userId={}, type={}, reason=notificationDisabled",
          receiver.getId(),
          notificationType);
      return;
    }
    if (notificationType.isMarketing() && !Boolean.TRUE.equals(receiver.getMarketingConsent())) {
      debugEvent(
          "NOTIFICATION_SKIPPED",
          "userId={}, type={}, reason=marketingConsentDisabled",
          receiver.getId(),
          notificationType);
      return;
    }

    Notification notification =
        notificationRepository.save(
            createNotification(receiver, sender, notificationType, url, thumbnailUrl));
    String receiverId = String.valueOf(receiver.getId());
    String eventId = receiverId + "_" + System.currentTimeMillis();
    Map<String, SseEmitter> emitters =
        emitterRepository.findAllEmitterStartWithByUserId(receiverId);
    debugEvent(
        "NOTIFICATION_CREATED",
        "id={}, userId={}, type={}, emitters={}",
        notification.getId(),
        receiver.getId(),
        notificationType,
        emitters.size());
    emitters.forEach(
        (key, emitter) -> {
          emitterRepository.saveEventCache(key, notification);
          sendNotification(emitter, eventId, key, NotificationResponse.from(notification));
        });
    sendPushNotification(receiver, notification);
  }

  private void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {
    try {
      emitter.send(SseEmitter.event().id(eventId).name("sse").data(data));
      debugEvent("SSE_SENT", "emitterId={}, eventId={}", emitterId, eventId);
    } catch (IOException exception) {
      emitterRepository.deleteById(emitterId);
      log.error("SSE 연결 오류!", exception);
    }
  }

  private boolean hasLostData(String lastEventId) {
    return !lastEventId.isEmpty();
  }

  private void sendLostData(String lastEventId, Long userId, String emitterId, SseEmitter emitter) {
    Map<String, Object> eventCaches =
        emitterRepository.findAllEventCacheStartWithByUserId(String.valueOf(userId));
    eventCaches.entrySet().stream()
        .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
        .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
  }

  private Notification createNotification(
      User receiver,
      User sender,
      NotificationType notificationType,
      String url,
      String thumbnailUrl) {
    String content;
    if (sender != null) {
      content = String.format(notificationType.getMessageTemplate(), sender.getNickname());
    } else {
      // sender가 없는 시스템 알림(예: 신고 알림)의 경우 템플릿 그대로 사용
      content = notificationType.getMessageTemplate();
    }
    return Notification.builder()
        .receiver(receiver)
        .notificationType(notificationType)
        .content(content)
        .url(url)
        .thumbnailUrl(thumbnailUrl)
        .isRead(false)
        .build();
  }

  private String makeTimeIncludeId(Long userId) {
    return userId + "_" + System.currentTimeMillis();
  }

  @Transactional(readOnly = true)
  public List<NotificationResponse> getNotifications(Long userId) {
    List<Notification> notifications =
        notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(userId);
    long unreadCount = notifications.stream().filter(notification -> !notification.isRead()).count();
    log.info(
        "[NOTIFICATION_FETCH] userId={}, totalCount={}, unreadCount={}",
        userId,
        notifications.size(),
        unreadCount);
    return notifications.stream().map(NotificationResponse::from).collect(Collectors.toList());
  }

  @Transactional
  public void readNotification(Long userId, Long notificationId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));
    if (!notification.getReceiver().getId().equals(userId)) {
      debugEvent(
          "READ_DENIED",
          "notificationId={}, userId={}, receiverId={}",
          notificationId,
          userId,
          notification.getReceiver().getId());
      throw new IllegalArgumentException("알림을 읽을 권한이 없습니다.");
    }
    notification.read();
    debugEvent("READ_OK", "notificationId={}, userId={}", notificationId, userId);
  }

  public void registerOrUpdateDeviceToken(Long userId, NotificationTokenRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

    DeviceToken deviceToken =
        deviceTokenRepository.findByUser(user).orElse(DeviceToken.builder().user(user).build());

    deviceToken.setToken(request.getToken());
    deviceTokenRepository.save(deviceToken);
  }

  public void updateNotificationSettings(Long userId, NotificationSettingsRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

    user.setNotificationEnabled(request.isNotificationEnabled());
    // [추가] 마케팅 수신 동의 설정 업데이트 - User 엔티티에 필드 추가 필요
    user.setMarketingConsent(request.isMarketingConsent());
    userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public com.example.cp_main_be.domain.member.notification.dto.response.NotificationSettingsResponse
      getNotificationSettings(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

    return com.example.cp_main_be.domain.member.notification.dto.response
        .NotificationSettingsResponse.builder()
        .notificationEnabled(user.getNotificationEnabled())
        .marketingConsent(user.getMarketingConsent() != null ? user.getMarketingConsent() : false)
        .build();
  }

  private void sendPushNotification(User receiver, Notification notification) {
    if (!Boolean.TRUE.equals(receiver.getNotificationEnabled())) {
      debugEvent(
          "PUSH_SKIPPED",
          "userId={}, type={}, reason=notificationDisabled",
          receiver.getId(),
          notification.getNotificationType());
      return;
    }
    Optional<DeviceToken> deviceTokenOptional = deviceTokenRepository.findByUser(receiver);
    if (deviceTokenOptional.isPresent()) {
      DeviceToken deviceToken = deviceTokenOptional.get();
      Message message =
          Message.builder()
              .setToken(deviceToken.getToken())
              .putData("title", notification.getNotificationType().name())
              .putData("body", notification.getContent())
              .putData("url", notification.getUrl())
              .build();
      try {
        FirebaseMessaging.getInstance().send(message);
        debugEvent(
            "PUSH_SENT",
            "userId={}, type={}, token={}",
            receiver.getId(),
            notification.getNotificationType(),
            maskToken(deviceToken.getToken()));
        log.info("푸시 알림 전송 성공: {}", notification.getContent());
      } catch (FirebaseMessagingException e) {
        if ("UNREGISTERED".equals(e.getMessagingErrorCode().name())) {
          log.warn(
              "Device token is no longer valid. Deleting token: {}",
              maskToken(deviceToken.getToken()));
          deviceTokenRepository.delete(deviceToken);
        } else {
          debugEvent(
              "PUSH_FAILED",
              "userId={}, type={}, token={}",
              receiver.getId(),
              notification.getNotificationType(),
              maskToken(deviceToken.getToken()));
          log.error("푸시 알림 전송 실패", e);
        }
      }
    } else {
      debugEvent("PUSH_SKIPPED", "userId={}, reason=missingToken", receiver.getId());
      log.debug("푸시 스킵: 디바이스 토큰 없음. userId={}", receiver.getId());
    }
  }

  @Scheduled(cron = "0 0 6 * * *")
  public void sendSunshineNotification() {
    List<User> users = userRepository.findAll(); // 모든 유저에게 보낼 경우
    for (User user : users) {
      if (!Boolean.TRUE.equals(user.getNotificationEnabled())) {
        debugEvent(
            "SCHEDULED_SKIPPED",
            "type=SUNSHINE, userId={}, reason=notificationDisabled",
            user.getId());
        continue;
      }
      send(user, user, NotificationType.SUNSHINE, "/garden", null);
    }
    log.info("Sending sunshine notification at {}", LocalDateTime.now());
  }

  @Scheduled(cron = "0 0 * * * *")
  public void updateDeviceTokenMetrics() {
    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    LocalDateTime startDate = now.minusDays(7);
    long totalUsers = userRepository.countByLastAccessedAtBetween(startDate, now);
    long tokenUsers = deviceTokenRepository.countDistinctActiveUserIdsWithToken(startDate, now);
    long missing = Math.max(0L, totalUsers - tokenUsers);
    double ratio = totalUsers == 0L ? 0.0d : (double) missing / (double) totalUsers;
    deviceTokenTotalUsers.set(totalUsers);
    deviceTokenMissingCount.set(missing);
    deviceTokenHasTokenCount.set(totalUsers - missing);
    deviceTokenMissingRatio.set(ratio);
    debugEvent(
        "DEVICE_TOKEN_METRICS_UPDATED",
        "totalUsers={}, tokenUsers={}, missing={}, ratio={}",
        totalUsers,
        tokenUsers,
        missing,
        ratio);
  }

  @Scheduled(cron = "0 0 12 * * *")
  public void sendPollenAvailableNotification() {
    List<User> users = userRepository.findAll();
    for (User user : users) {
      if (!Boolean.TRUE.equals(user.getNotificationEnabled())) {
        debugEvent(
            "SCHEDULED_SKIPPED",
            "type=POLLEN_AVAILABLE, userId={}, reason=notificationDisabled",
            user.getId());
        continue;
      }
      send(user, user, NotificationType.POLLEN_AVAILABLE, "/friends", null);
    }
    log.info("Sending pollen available notification at {}", LocalDateTime.now());
  }

  @Scheduled(cron = "0 0 0/8 * * *")
  public void sendWateringNotification() {
    List<User> users = userRepository.findAll();
    for (User user : users) {
      if (!Boolean.TRUE.equals(user.getNotificationEnabled())) {
        debugEvent(
            "SCHEDULED_SKIPPED",
            "type=WATERING, userId={}, reason=notificationDisabled",
            user.getId());
        continue;
      }
      String plantNickname = resolvePlantNickname(user);
      String content = String.format(NotificationType.WATERING.getMessageTemplate(), plantNickname);
      Notification notification =
          Notification.builder()
              .receiver(user)
              .notificationType(NotificationType.WATERING)
              .content(content)
              .url("/garden")
              .isRead(false)
              .build();
      notificationRepository.save(notification);
      sendPushNotification(user, notification);
    }
    log.info("Sending watering notification at {}", LocalDateTime.now());
  }

  private String resolvePlantNickname(User user) {
    String fallback = "당신의 식물";
    Garden garden = null;
    Long lastVisitedGardenId = user.getLastVisitedGardenId();
    if (lastVisitedGardenId != null) {
      garden =
          gardenRepository
              .findById(lastVisitedGardenId)
              .filter(g -> g.getUser().getId().equals(user.getId()))
              .orElse(null);
    }
    if (garden == null) {
      garden =
          gardenRepository.findFirstByUserAndIsLockedIsFalseOrderBySlotNumberAsc(user).orElse(null);
    }
    if (garden == null || garden.getAvatar() == null) {
      return fallback;
    }
    String nickname = garden.getAvatar().getNickname();
    if (nickname == null || nickname.isBlank()) {
      return fallback;
    }
    return nickname;
  }

  private void debugEvent(String event, String message, Object... args) {
    if (shouldDebug()) {
      log.debug("event={}, " + message, prepend(event, args));
    }
  }

  private Object[] prepend(Object first, Object[] rest) {
    Object[] merged = new Object[rest.length + 1];
    merged[0] = first;
    System.arraycopy(rest, 0, merged, 1, rest.length);
    return merged;
  }

  private boolean shouldDebug() {
    if (!notificationDebugEnabled || !log.isDebugEnabled()) {
      return false;
    }
    double rate = notificationDebugSampleRate;
    if (rate >= 1.0d) {
      return true;
    }
    if (rate <= 0.0d) {
      return false;
    }
    return ThreadLocalRandom.current().nextDouble() < rate;
  }

  private String maskToken(String token) {
    if (token == null || token.isBlank()) {
      return "null";
    }
    if (token.length() <= 8) {
      return "****";
    }
    return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
  }
}
