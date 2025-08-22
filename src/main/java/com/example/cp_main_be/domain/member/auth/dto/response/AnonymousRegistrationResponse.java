package com.example.cp_main_be.domain.member.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnonymousRegistrationResponse {

  private final String accessToken;
  private final String refreshToken;
  private final String nickname; // [추가] 생성된 닉네임
  private final boolean isNewUser;
}
