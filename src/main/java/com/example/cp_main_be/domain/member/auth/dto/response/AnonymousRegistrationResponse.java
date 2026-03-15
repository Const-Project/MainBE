package com.example.cp_main_be.domain.member.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnonymousRegistrationResponse {

  private final String accessToken;
  private final String refreshToken;
  private final Long userId;
  private final String nickname;
  private final boolean isNewUser;
  private final boolean requiresNicknameSetup;
}
