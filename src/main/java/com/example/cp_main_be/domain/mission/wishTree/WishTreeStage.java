package com.example.cp_main_be.domain.mission.wishTree;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WishTreeStage {
  // 다음 단계로 가기 위해 필요한 '누적' 포인트와 이름 정의
  SPROUT(1000, "새싹", 1),
  FLOWER(2150, "꽃", 2), // 1000 + 1150
  FRUIT(3450, "열매", 3), // 2150 + 1300
  TREE(4900, "나무", 4), // 3450 + 1450
  FINAL(Integer.MAX_VALUE, "최종 나무", 5); // 더 이상 성장 안함

  private final int requiredPointsForNextStage;
  private final String koreanName;
  private final int maxGardens;

  public static WishTreeStage getStageForPoints(int points) {
    if (points < SPROUT.requiredPointsForNextStage) return SPROUT;
    if (points < FLOWER.requiredPointsForNextStage) return FLOWER;
    if (points < FRUIT.requiredPointsForNextStage) return FRUIT;
    if (points < TREE.requiredPointsForNextStage) return TREE;
    return FINAL;
  }

  public int getMaxGardens(WishTreeStage stage) {
    return stage.maxGardens;
  }
}
