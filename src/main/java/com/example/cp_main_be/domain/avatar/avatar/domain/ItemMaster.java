package com.example.cp_main_be.domain.avatar.avatar.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "item_master")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemMaster {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "item_master_id")
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 500)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private ItemType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ItemRarity rarity;

  @Column(name = "image_url", length = 500)
  private String imageUrl;

  @Column(name = "thumbnail_url", length = 500)
  private String thumbnailUrl;

  @Column(nullable = false)
  @Builder.Default
  private Integer price = 0;

  @Column(name = "required_level")
  @Builder.Default
  private Integer requiredLevel = 1;

  @Column(nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "sort_order")
  @Builder.Default
  private Integer sortOrder = 0;

  // 스탯 보너스 (선택적)
  @Column(name = "stat_bonus_hp")
  @Builder.Default
  private Integer statBonusHp = 0;

  @Column(name = "stat_bonus_attack")
  @Builder.Default
  private Integer statBonusAttack = 0;

  @Column(name = "stat_bonus_defense")
  @Builder.Default
  private Integer statBonusDefense = 0;

  // 특별 효과 (JSON으로 저장하거나 별도 테이블로 관리)
  @Column(name = "special_effects", columnDefinition = "TEXT")
  private String specialEffects;

  // 이벤트 아이템 여부
  @Column(name = "is_event_item")
  @Builder.Default
  private Boolean isEventItem = false;

  // 이벤트 기간 (이벤트 아이템인 경우)
  @Column(name = "event_start_date")
  private LocalDateTime eventStartDate;

  @Column(name = "event_end_date")
  private LocalDateTime eventEndDate;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // 비즈니스 로직 메서드
  public boolean isEquippable() {
    return isActive
        && (type == ItemType.HAT
            || type == ItemType.CLOTHES
            || type == ItemType.SHOES
            || type == ItemType.ACCESSORY);
  }

  public boolean isUsable() {
    return isActive && (type == ItemType.CONSUMABLE || type == ItemType.TOOL);
  }

  public boolean isEventActive() {
    if (!isEventItem) return false;

    LocalDateTime now = LocalDateTime.now();
    return (eventStartDate == null || now.isAfter(eventStartDate))
        && (eventEndDate == null || now.isBefore(eventEndDate));
  }

  public String getRarityDisplayName() {
    return switch (rarity) {
      case COMMON -> "일반";
      case UNCOMMON -> "고급";
      case RARE -> "희귀";
      case EPIC -> "영웅";
      case LEGENDARY -> "전설";
    };
  }

  public String getTypeDisplayName() {
    return switch (type) {
      case HAT -> "모자";
      case CLOTHES -> "의상";
      case SHOES -> "신발";
      case ACCESSORY -> "악세서리";
      case CONSUMABLE -> "소모품";
      case TOOL -> "도구";
      case DECORATION -> "장식품";
      case SPECIAL -> "특수아이템";
    };
  }
}
