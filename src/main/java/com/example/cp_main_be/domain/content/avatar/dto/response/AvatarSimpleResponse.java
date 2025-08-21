package com.example.cp_main_be.domain.content.avatar.dto.response;

import com.example.cp_main_be.domain.content.avatar.domain.Avatar;
import lombok.Getter;

@Getter
public class AvatarSimpleResponse {
  private Long id;
  private String name;
  private String imageUrl;

  // Avatar 엔티티를 AvatarDto로 변환하는 생성자
  public AvatarSimpleResponse(Avatar avatar) {
    this.id = avatar.getId();
    this.name = avatar.getName();
    this.imageUrl = avatar.getImageUrl();
  }
}
