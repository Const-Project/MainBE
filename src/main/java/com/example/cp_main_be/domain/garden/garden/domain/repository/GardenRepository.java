package com.example.cp_main_be.domain.garden.garden.domain.repository;

import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.member.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GardenRepository extends JpaRepository<Garden, Long> {
  // [추가] User로 Garden을 조회할 때, 연관된 Avatar와 GardenBackground를 즉시 함께 로딩하는 메서드
  @Query(
      "SELECT g FROM Garden g "
          + "JOIN FETCH g.avatar "
          + "JOIN FETCH g.gardenBackground "
          + "WHERE g.user = :user")
  Optional<Garden> findByUserWithDetails(@Param("user") User user);

  // 유저의 정원 중 잠겨있으면서 가장 낮은 슬롯 번호를 가진 정원 1개를 찾기
  Optional<Garden> findFirstByUserAndIsLockedIsTrueOrderBySlotNumberAsc(User user);

  // 유저의 해금된 정원 개수 세기
  long countByUserAndIsLockedIsFalse(User user);

  Optional<Garden> findFirstByUserAndIsLockedIsFalseOrderBySlotNumberAsc(User user);

  Optional<Garden> findFirstByUserAndIsLockedIsFalseAndAvatarIsNullOrderBySlotNumberAsc(User user);
}
