package com.example.cp_main_be.domain.member.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LevelStatusResponseDto {
  private final int level;
  private final long currentExp;
  private final long requiredExpForNextLevel;
  private final long totalPoints;
}
