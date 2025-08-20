package com.example.cp_main_be.domain.member.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NicknameChangeRequest {
  @NotBlank(message = "새로운 닉네임은 필수입니다.")
  private String newNickname;
}
