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
    private Long level ; // 레벨
    private Integer temperatureScore; // 온도 점수
  }
}
