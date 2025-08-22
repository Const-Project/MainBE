package com.example.cp_main_be.domain.avatar.avatar.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemType {
  // 장착 가능한 아이템들
  HAT("HAT", "모자", true),
  CLOTHES("CLOTHES", "의상", true),
  SHOES("SHOES", "신발", true),
  ACCESSORY("ACCESSORY", "악세서리", true),

  // 사용 가능한 아이템들
  CONSUMABLE("CONSUMABLE", "소모품", false),
  TOOL("TOOL", "도구", false),

  // 기타 아이템들
  DECORATION("DECORATION", "장식품", false),
  SPECIAL("SPECIAL", "특수아이템", false);

  private final String code;
  private final String displayName;
  private final boolean equippable;

  public static ItemType fromCode(String code) {
    for (ItemType type : values()) {
      if (type.getCode().equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown ItemType code: " + code);
  }
}
