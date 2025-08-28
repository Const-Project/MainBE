package com.example.cp_main_be.domain.delivery.dto.response;

import com.example.cp_main_be.domain.delivery.domain.DeliveryPlant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeliveryPlantResponse {

  private Long seedType;
  private String imageUrl;
  private String name;

  public static DeliveryPlantResponse from(DeliveryPlant deliveryPlant) {
    return DeliveryPlantResponse.builder()
        .seedType(deliveryPlant.getId())
        .imageUrl(deliveryPlant.getImageUrl())
        .name(deliveryPlant.getName())
        .build();
  }
}
