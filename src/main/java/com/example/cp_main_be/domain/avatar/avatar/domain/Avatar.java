package com.example.cp_main_be.domain.avatar.avatar.domain;

import static com.example.cp_main_be.domain.avatar.avatar.domain.ItemType.*;

import com.example.cp_main_be.domain.member.user.domain.User;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Avatar {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "avatar_id")
  private Long id;

  @Column(nullable = false)
  private String nickname;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "master_id", nullable = false)
  private AvatarMaster avatarMaster;

  // 각 카테고리별로 하나씩만 장착 가능한 아이템들
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "equipped_hat_id")
  private ItemMaster equippedHat;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "equipped_clothes_id")
  private ItemMaster equippedClothes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "equipped_shoes_id")
  private ItemMaster equippedShoes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "equipped_accessory_id")
  private ItemMaster equippedAccessory;

  // 비즈니스 로직 메서드들
  public void equipItem(ItemMaster item) {
    if (item.getType() == HAT) {
      this.equippedHat = item;
    } else if (item.getType() == CLOTHES) {
      this.equippedClothes = item;
    } else if (item.getType() == SHOES) {
      this.equippedShoes = item;
    } else if (item.getType() == ACCESSORY) {
      this.equippedAccessory = item;
    } else {
      throw new IllegalArgumentException("장착할 수 없는 아이템 타입: " + item.getType());
    }
  }

  public void unequipItem(ItemType itemType) {
    switch (itemType) {
      case HAT -> this.equippedHat = null;
      case CLOTHES -> this.equippedClothes = null;
      case SHOES -> this.equippedShoes = null;
      case ACCESSORY -> this.equippedAccessory = null;
    }
  }

  public ItemMaster getEquippedItem(ItemType itemType) {
    return switch (itemType) {
      case HAT -> this.equippedHat;
      case CLOTHES -> this.equippedClothes;
      case SHOES -> this.equippedShoes;
      case ACCESSORY -> this.equippedAccessory;
      default -> null;
    };
  }

  public boolean hasEquippedItem(ItemType itemType) {
    return getEquippedItem(itemType) != null;
  }

  // 장착된 모든 아이템 리스트 반환 (HomeService용)
  public List<ItemMaster> getEquippedItems() {
    List<ItemMaster> items = new ArrayList<>();
    if (equippedHat != null) items.add(equippedHat);
    if (equippedClothes != null) items.add(equippedClothes);
    if (equippedShoes != null) items.add(equippedShoes);
    if (equippedAccessory != null) items.add(equippedAccessory);
    return items;
  }

  public int getEquippedItemsCount() {
    int count = 0;
    if (equippedHat != null) count++;
    if (equippedClothes != null) count++;
    if (equippedShoes != null) count++;
    if (equippedAccessory != null) count++;
    return count;
  }
}
