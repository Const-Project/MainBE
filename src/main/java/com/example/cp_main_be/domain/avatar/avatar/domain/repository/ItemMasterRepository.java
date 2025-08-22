package com.example.cp_main_be.domain.avatar.avatar.domain.repository;

import com.example.cp_main_be.domain.avatar.avatar.domain.ItemMaster;
import com.example.cp_main_be.domain.avatar.avatar.domain.ItemRarity;
import com.example.cp_main_be.domain.avatar.avatar.domain.ItemType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemMasterRepository extends JpaRepository<ItemMaster, Long> {

  // 활성화된 아이템만 조회
  List<ItemMaster> findByIsActiveTrueOrderBySortOrderAsc();

  // 타입별 조회
  List<ItemMaster> findByTypeAndIsActiveTrueOrderBySortOrderAsc(ItemType type);

  // 희귀도별 조회
  List<ItemMaster> findByRarityAndIsActiveTrueOrderBySortOrderAsc(ItemRarity rarity);

  // 장착 가능한 아이템만 조회
  @Query(
      "SELECT im FROM ItemMaster im WHERE im.isActive = true AND im.type IN ('HAT', 'CLOTHES', 'SHOES', 'ACCESSORY') ORDER BY im.sortOrder ASC")
  List<ItemMaster> findEquippableItems();

  // 가격 범위로 조회
  List<ItemMaster> findByIsActiveTrueAndPriceBetweenOrderByPriceAsc(
      Integer minPrice, Integer maxPrice);

  // 필요 레벨 이하 아이템 조회
  List<ItemMaster> findByIsActiveTrueAndRequiredLevelLessThanEqualOrderBySortOrderAsc(
      Integer userLevel);

  // 이름으로 검색 (부분 일치)
  List<ItemMaster> findByIsActiveTrueAndNameContainingIgnoreCaseOrderBySortOrderAsc(String name);

  // 이벤트 아이템 조회
  @Query(
      "SELECT im FROM ItemMaster im WHERE im.isActive = true AND im.isEventItem = true "
          + "AND (im.eventStartDate IS NULL OR im.eventStartDate <= CURRENT_TIMESTAMP) "
          + "AND (im.eventEndDate IS NULL OR im.eventEndDate >= CURRENT_TIMESTAMP) "
          + "ORDER BY im.sortOrder ASC")
  List<ItemMaster> findActiveEventItems();

  // 타입과 희귀도로 조회
  List<ItemMaster> findByTypeAndRarityAndIsActiveTrueOrderBySortOrderAsc(
      ItemType type, ItemRarity rarity);

  // ID 리스트로 조회
  @Query(
      "SELECT im FROM ItemMaster im WHERE im.id IN :ids AND im.isActive = true ORDER BY im.sortOrder ASC")
  List<ItemMaster> findByIdsAndIsActiveTrue(@Param("ids") List<Long> ids);

  // 상점용 아이템 조회 (이벤트 아이템 제외)
  @Query(
      "SELECT im FROM ItemMaster im WHERE im.isActive = true AND im.isEventItem = false "
          + "AND im.requiredLevel <= :userLevel ORDER BY im.type ASC, im.rarity ASC, im.sortOrder ASC")
  List<ItemMaster> findShopItems(@Param("userLevel") Integer userLevel);

  // 특정 타입의 기본 아이템 조회 (가장 저렴한 아이템)
  @Query(
      "SELECT im FROM ItemMaster im WHERE im.type = :type AND im.isActive = true "
          + "ORDER BY im.price ASC, im.sortOrder ASC")
  Optional<ItemMaster> findCheapestByType(@Param("type") ItemType type);

  // 통계용 쿼리들
  @Query("SELECT COUNT(im) FROM ItemMaster im WHERE im.isActive = true")
  long countActiveItems();

  @Query("SELECT COUNT(im) FROM ItemMaster im WHERE im.type = :type AND im.isActive = true")
  long countByType(@Param("type") ItemType type);

  @Query(
      "SELECT im.rarity, COUNT(im) FROM ItemMaster im WHERE im.isActive = true GROUP BY im.rarity")
  List<Object[]> countByRarity();
}
