package com.example.cp_main_be.domain.delivery.dto.response;

import com.example.cp_main_be.domain.delivery.domain.Delivery;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DeliveryResponse {
  private Long seedType;

  private String recipientName;

  private String recipientPhone;

  private String postalCode;

  private String address;

  private String addressDetail;
  private String message;

  public static DeliveryResponse from(Delivery delivery) {
    return DeliveryResponse.builder()
        .seedType(delivery.getSeedType())
        .recipientName(delivery.getRecipientName())
        .recipientPhone(delivery.getRecipientPhone())
        .postalCode(delivery.getPostalCode())
        .address(delivery.getAddress())
        .addressDetail(delivery.getAddressDetail())
        .message(delivery.getMessage())
        .build();
  }
}
