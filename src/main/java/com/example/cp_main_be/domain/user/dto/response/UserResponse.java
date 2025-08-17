package com.example.cp_main_be.domain.user.dto.response;

import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private Long id;
  private String username;
  private UUID uuid;
  private String accessToken;
  private String refreshToken;

  @Getter
  @Builder
  public static class LevelStatusResponseDTO {
    // GET /level 의 Response
    private Integer level ; // 레벨
    private Integer experience; // 경험
  }
}
