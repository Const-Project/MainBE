package com.example.cp_main_be.domain.member.user.dto.response;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterResponse {

  private Long id;
  private String username;
  private UUID uuid;
  private String accessToken;
  private String refreshToken;
}
