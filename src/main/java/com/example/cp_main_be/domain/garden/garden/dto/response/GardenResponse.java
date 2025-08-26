package com.example.cp_main_be.domain.garden.garden.dto.response;

import com.example.cp_main_be.domain.avatar.avatar.dto.response.AvatarSimpleResponse;
import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GardenResponse {

  private final Long id;
  private final Long userId;
  private final Integer slotNumber;
  private final Integer waterCount;
  private final Integer sunlightCount;
  private final AvatarSimpleResponse avatar;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;

  @Builder
  private GardenResponse(
      Long id,
      Long userId,
      Integer slotNumber,
      Integer waterCount,
      Integer sunlightCount,
      AvatarSimpleResponse avatar,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.userId = userId;
    this.slotNumber = slotNumber;
    this.waterCount = waterCount;
    this.sunlightCount = sunlightCount;
    this.avatar = avatar;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static GardenResponse from(Garden garden) {
    return GardenResponse.builder()
        .id(garden.getId())
        .userId(garden.getUser().getId())
        .slotNumber(garden.getSlotNumber())
        .waterCount(garden.getWaterCount())
        .sunlightCount(garden.getSunlightCount())
        .avatar(AvatarSimpleResponse.from(garden.getAvatar()))
        .createdAt(garden.getCreatedAt())
        .updatedAt(garden.getUpdatedAt())
        .build();
  }
}
