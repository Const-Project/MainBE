package com.example.cp_main_be.domain.member.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SupabaseLoginRequest {
  @NotBlank(message = "accessToken은 필수입니다")
  private String accessToken;
}
