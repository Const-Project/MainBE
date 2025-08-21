package com.example.cp_main_be.domain.delivery.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 배송을 요청한 사용자

  @Column(nullable = false)
  private String recipientName;

  @Column(nullable = false)
  private String recipientPhone;

  @Column(nullable = false)
  private String postalCode;

  @Column(nullable = false)
  private String address;

  private String addressDetail;
  private String message;

  // 배송 상태를 관리하기 위한 Enum (추후 확장용)
  // @Enumerated(EnumType.STRING)
  // private DeliveryStatus status;

  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
    // this.status = DeliveryStatus.REQUESTED;
  }

  @Builder
  public Delivery(
      User user,
      String recipientName,
      String recipientPhone,
      String postalCode,
      String address,
      String addressDetail,
      String message) {
    this.user = user;
    this.recipientName = recipientName;
    this.recipientPhone = recipientPhone;
    this.postalCode = postalCode;
    this.address = address;
    this.addressDetail = addressDetail;
    this.message = message;
  }
}
