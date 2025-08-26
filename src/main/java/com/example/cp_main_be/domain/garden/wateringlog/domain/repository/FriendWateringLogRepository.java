package com.example.cp_main_be.domain.garden.wateringlog.domain.repository;

import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.wateringlog.domain.FriendWateringLog;
import com.example.cp_main_be.domain.member.user.domain.User;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendWateringLogRepository extends JpaRepository<FriendWateringLog, Long> {

  int countByWaterGiverAndWateredAtAfter(User waterGiver, LocalDateTime startOfDay);

  boolean existsByWaterGiverAndWateredGardenAndWateredAtAfter(
      User waterGiver, Garden wateredGarden, LocalDateTime startOfDay);

  /**
   * 지정된 날짜 이전의 모든 물주기 로그를 삭제합니다. (벌크 삭제)
   *
   * @return 삭제된 레코드의 수
   */
  @Modifying
  @Query("DELETE FROM FriendWateringLog fwl WHERE fwl.wateredAt < :cutoffDate")
  int deleteByWateredAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
