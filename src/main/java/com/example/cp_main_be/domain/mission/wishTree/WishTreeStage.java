package com.example.cp_main_be.domain.mission.wishTree;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WishTreeStage {
  // 레벨, 해당 레벨 시작 누적 경험치, 다음 레벨 시작 누적 경험치, 한글이름, 해금되는 정원 수
  SPROUT(1, 0L, 1000L, "새싹", 1L),
  FLOWER(2, 1000L, 2150L, "꽃", 2L),
  FRUIT(3, 2150L, 3450L, "열매", 3L),
  TREE(4, 3450L, 4900L, "나무", 4L),
  FINAL(5, 4900L, Long.MAX_VALUE, "최종 나무", 4L);

  private final int level;
  private final Long requiredPoints; // 이 단계 시작에 필요한 누적 경험치
  private final Long requiredPointsForNextStage; // 다음 단계 시작에 필요한 누적 경험치
  private final String koreanName;
  private final Long maxGardens;

  public WishTreeStage getNextStage() {
    if (this == FINAL) {
      return null; // 마지막 단계에서는 다음 단계 없음
    }
    return values()[this.ordinal() + 1];
  }
}
