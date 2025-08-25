package com.example.cp_main_be.domain.avatar.avatar.dto.response;

import com.example.cp_main_be.domain.avatar.avatar.domain.AvatarMaster;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvatarMasterResponse {
  private final Long id;
  private final String defaultImageUrl;
  private final String description;

  public static AvatarMasterResponse from(AvatarMaster master) {
    return AvatarMasterResponse.builder()
        .id(master.getId())
        .defaultImageUrl(master.getDefaultImageUrl())
        .description(master.getDescription())
        .build();
  }
}
