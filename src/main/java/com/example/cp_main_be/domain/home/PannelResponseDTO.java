package com.example.cp_main_be.domain.home;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PannelResponseDTO {
  Boolean isDairyCompleted;
  Boolean isCheckingCompleted;
  Boolean isQuizCompleted;
  WishTreeDto wishTree;

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class WishTreeDto {
    String currentStage;
    String nextStage;
    Long currentPoints;
    Long requiredPointsForNextStage;
    Long progressPercent;
  }
}
