package com.example.cp_main_be.domain.delivery.dto.response;

import com.example.cp_main_be.domain.delivery.domain.DeliveryPlant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeliveryPlantResponse {

  private Long id;
  private Long seedType;
  private String imageUrl;
  private String name;

  public static DeliveryPlantResponse from(DeliveryPlant deliveryPlant) {
    return DeliveryPlantResponse.builder()
        .id(deliveryPlant.getId())
        .seedType(deliveryPlant.getSeedType())
        .imageUrl(deliveryPlant.getImageUrl())
        .name(deliveryPlant.getName())
        .build();
  }
}
