package com.example.cp_main_be.domain.member.notification.service;

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
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final EmitterRepository emitterRepository;
  private final NotificationRepository notificationRepository;

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

    return emitter;
  }

  public void send(User receiver, User sender, NotificationType notificationType, String url) {
    Notification notification =
        notificationRepository.save(createNotification(receiver, sender, notificationType, url));
    String receiverId = String.valueOf(receiver.getId());
    String eventId = receiverId + "_" + System.currentTimeMillis();
    Map<String, SseEmitter> emitters =
        emitterRepository.findAllEmitterStartWithByUserId(receiverId);
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
      User receiver, User sender, NotificationType notificationType, String url) {
    String content = String.format(notificationType.getMessageTemplate(), sender.getUsername());
    return Notification.builder()
        .receiver(receiver)
        .notificationType(notificationType)
        .content(content)
        .url(url)
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
    return notifications.stream().map(NotificationResponse::from).collect(Collectors.toList());
  }

  @Transactional
  public void readNotification(Long notificationId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));
    notification.read();
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
    // TODO: User 엔티티에 알림 설정 필드 추가 후 로직 구현
    // user.setNotificationEnabled(request.isNotificationEnabled());
    // userRepository.save(user);
  }

  private void sendPushNotification(User receiver, Notification notification) {
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
        log.info("푸시 알림 전송 성공: {}", notification.getContent());
      } catch (FirebaseMessagingException e) {
        if ("UNREGISTERED".equals(e.getMessagingErrorCode().name())) {
          log.warn("Device token is no longer valid. Deleting token: {}", deviceToken.getToken());
          deviceTokenRepository.delete(deviceToken);
        } else {
          log.error("푸시 알림 전송 실패", e);
        }
      }
    } else {
      log.warn("디바이스 토큰을 찾을 수 없어 푸시 알림을 전송할 수 없습니다. userId: {}", receiver.getId());
    }
  }

  @Scheduled(cron = "0 0 6 * * *")
  public void sendSunshineNotification() {
    List<User> users = userRepository.findAll(); // 모든 유저에게 보낼 경우
    for (User user : users) {
      send(user, user, NotificationType.SUNSHINE, "/garden");
    }
    log.info("Sending sunshine notification at {}", LocalDateTime.now());
  }

  @Scheduled(cron = "0 0 12 * * *")
  public void sendPollenAvailableNotification() {
    List<User> users = userRepository.findAll();
    for (User user : users) {
      send(user, user, NotificationType.POLLEN_AVAILABLE, "/friends");
    }
    log.info("Sending pollen available notification at {}", LocalDateTime.now());
  }

  @Scheduled(cron = "0 0 0/8 * * *")
  public void sendWateringNotification() {
    List<User> users = userRepository.findAll();
    for (User user : users) {
      // TODO: 식물 닉네임 가져오는 로직 필요
      String plantNickname = "당신의 식물";
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
}
