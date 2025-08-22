package com.example.cp_main_be.domain.member.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

  //  private UUID uuid;

  @NotBlank(message = "유저 이름은 필수입니다.")
  private String username;

  //  private String avatarUrl;
}
