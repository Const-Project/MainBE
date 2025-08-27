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
  TREE(4900L, "나무", 4L), // 3450 + 1450, 최대 텃밭 개수
  FINAL(Long.MAX_VALUE, "최종 나무", 4L); // 더 이상 성장 안함, 최대 텃밭 개수

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

  public WishTreeStage getNextStage() {
    if (this == FINAL) {
      return null; // 마지막 단계에서는 다음 단계 없음
    }
    // values()는 Enum 상수가 선언된 순서대로 배열을 반환합니다.
    // 현재 단계의 순서(ordinal)에 +1을 하여 다음 단계를 찾습니다.
    return values()[this.ordinal() + 1];
  }

  /**
   * 현재 단계에서 가질 수 있는 최대 텃밭 개수를 반환합니다.
   *
   * @return 최대 텃밭 개수
   */
  public Long getMaxGardens() {
    return this.maxGardens;
  }
}
