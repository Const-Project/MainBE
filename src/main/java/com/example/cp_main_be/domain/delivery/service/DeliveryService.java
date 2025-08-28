package com.example.cp_main_be.domain.delivery.service;

import com.example.cp_main_be.domain.delivery.domain.Delivery;
import com.example.cp_main_be.domain.delivery.domain.repository.DeliveryPlantRepository;
import com.example.cp_main_be.domain.delivery.domain.repository.DeliveryRepository;
import com.example.cp_main_be.domain.delivery.dto.request.DeliveryRequest;
import com.example.cp_main_be.domain.delivery.dto.response.DeliveryPlantResponse;
import com.example.cp_main_be.domain.delivery.dto.response.DeliveryResponse;
import com.example.cp_main_be.domain.member.notification.domain.NotificationType;
import com.example.cp_main_be.domain.member.notification.service.NotificationService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {

  private final DeliveryRepository deliveryRepository;
  private final UserRepository userRepository;
  private final DeliveryPlantRepository deliveryPlantRepository;
  private final NotificationService notificationService; // 👈 NotificationService 주입

  public void createDeliveryRequest(Long userId, DeliveryRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

    Delivery delivery =
        Delivery.builder()
            .user(user)
            .recipientName(request.getRecipientName())
            .recipientPhone(request.getRecipientPhone())
            .postalCode(request.getPostalCode())
            .address(request.getAddress())
            .addressDetail(request.getAddressDetail())
            .seedType(request.getSeedType())
            .message(request.getMessage())
            .build();

    Delivery savedDelivery = deliveryRepository.save(delivery); // 👈 저장 후 객체 받기

    // 👈 알림 전송 로직 추가
    // 알림을 받는 사람(owner)과 보내는 사람(writer)이 자기 자신인 시스템 알림
    notificationService.send(
        user,
        user,
        NotificationType.SEED_DELIVERY,
        "/deliveries/" + savedDelivery.getId() // 배송 상세 조회 페이지 URL
        );
  }

  public List<DeliveryResponse> getMyDelivery(User user) {
    return deliveryRepository.findByUserId(user.getId()).stream()
        .map(DeliveryResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<DeliveryPlantResponse> getDeliveryPlantList() {
    return deliveryPlantRepository.findAll().stream()
        .map(DeliveryPlantResponse::from)
        .collect(Collectors.toList());
  }
}
