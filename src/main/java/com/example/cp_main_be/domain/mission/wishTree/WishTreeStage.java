package com.example.cp_main_be.domain.mission.wishTree;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WishTreeStage {
  // 다음 단계로 가기 위해 필요한 '누적' 포인트와 이름 정의
  SPROUT(1000L, "새싹", 1L),
  FLOWER(2150L, "꽃", 2L), // 1000 + 1150
  FRUIT(3450L, "열매", 3L), // 2150 + 1300
  TREE(4900L, "나무", 4L), // 3450 + 1450
  FINAL(Long.MAX_VALUE, "최종 나무", 5L); // 더 이상 성장 안함

  private final Long requiredPointsForNextStage;
  private final String koreanName;
  private final Long maxGardens;

  public static WishTreeStage getStageForPoints(Long points) {
    if (points < SPROUT.requiredPointsForNextStage) return SPROUT;
    if (points < FLOWER.requiredPointsForNextStage) return FLOWER;
    if (points < FRUIT.requiredPointsForNextStage) return FRUIT;
    if (points < TREE.requiredPointsForNextStage) return TREE;
    return FINAL;
  }

  public Long getMaxGardens(WishTreeStage stage) {
    return stage.maxGardens;
  }
}
