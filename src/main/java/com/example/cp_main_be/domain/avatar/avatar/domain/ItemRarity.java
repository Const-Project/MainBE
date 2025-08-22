package com.example.cp_main_be.domain.avatar.avatar.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemRarity {
  COMMON("COMMON", "일반", "#9CA3AF", 1),
  UNCOMMON("UNCOMMON", "고급", "#10B981", 2),
  RARE("RARE", "희귀", "#3B82F6", 3),
  EPIC("EPIC", "영웅", "#8B5CF6", 4),
  LEGENDARY("LEGENDARY", "전설", "#F59E0B", 5);

  private final String code;
  private final String displayName;
  private final String colorCode; // UI에서 사용할 색상
  private final int sortOrder; // 정렬 순서

  public static ItemRarity fromCode(String code) {
    for (ItemRarity rarity : values()) {
      if (rarity.getCode().equals(code)) {
        return rarity;
      }
    }
    throw new IllegalArgumentException("Unknown ItemRarity code: " + code);
  }

  public boolean isHigherThan(ItemRarity other) {
    return this.sortOrder > other.sortOrder;
  }

  public boolean isLowerThan(ItemRarity other) {
    return this.sortOrder < other.sortOrder;
  }
}
