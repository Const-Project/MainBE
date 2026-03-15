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

  @Query(
      "SELECT COUNT(fwl) FROM FriendWateringLog fwl "
          + "WHERE fwl.waterGiver = :waterGiver "
          + "AND fwl.wateredGarden.user = :targetUser "
          + "AND fwl.wateredAt >= :startOfDay")
  long countByWaterGiverAndTargetUserAndWateredAtAfter(
      @Param("waterGiver") User waterGiver,
      @Param("targetUser") User targetUser,
      @Param("startOfDay") LocalDateTime startOfDay);

  boolean existsByWaterGiverAndWateredGardenAndWateredAtAfter(
      User waterGiver, Garden wateredGarden, LocalDateTime startOfDay);

  @Modifying
  @Query("DELETE FROM FriendWateringLog fwl WHERE fwl.wateredAt < :cutoffDate")
  int deleteByWateredAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
