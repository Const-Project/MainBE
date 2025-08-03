package com.example.cp_main_be.domain.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationTokenRequest {
  @NotBlank(message = "디바이스 토큰은 필수입니다.")
  private String token;
}
