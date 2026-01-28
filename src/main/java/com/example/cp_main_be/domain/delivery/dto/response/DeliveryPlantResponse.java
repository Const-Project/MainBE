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

  @io.swagger.v3.oas.annotations.media.Schema(description = "해금 레벨 (이 레벨 이상이어야 선택 가능)")
  private Integer unlockLevel;

  public static DeliveryPlantResponse from(DeliveryPlant deliveryPlant) {
    return DeliveryPlantResponse.builder()
        .seedType(deliveryPlant.getId())
        .imageUrl(deliveryPlant.getImageUrl())
        .name(deliveryPlant.getName())
        .unlockLevel(deliveryPlant.getUnlockLevel())
        .build();
  }
}
