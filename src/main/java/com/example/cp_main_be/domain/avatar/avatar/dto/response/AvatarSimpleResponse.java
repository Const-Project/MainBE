package com.example.cp_main_be.domain.avatar.avatar.dto.response;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvatarSimpleResponse {
  private Long id;
  private String name;
  private String imageUrl;

  public static AvatarSimpleResponse from(Avatar avatar) {
    return AvatarSimpleResponse.builder()
        .id(avatar.getId())
        .name(avatar.getNickname())
        .imageUrl(avatar.getImageUrl())
        .build();
  }
}
