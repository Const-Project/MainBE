package com.example.cp_main_be.domain.member.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvatarChangeRequest {
  @NotBlank(message = "새로운 아바타 URL은 필수입니다.")
  private String newAvatarUrl;
}
