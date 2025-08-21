package com.example.cp_main_be.domain.mission.user_daily_mission.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserDailyMission;
import com.example.cp_main_be.domain.mission.user_daily_mission.dto.MissionCountPerDay;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserDailyMissionRepository extends JpaRepository<UserDailyMission, Long> {
  List<UserDailyMission> findAllByUser_Id(Long userId);

  @Query(
      "SELECT new com.example.cp_main_be.dto.MissionCountPerDay(DAY(udm.completedAt), COUNT(udm.id)) "
          + "FROM UserDailyMission udm "
          + "WHERE udm.user = :user "
          + "  AND udm.isCompleted = true "
          + // 1. 완료된 미션만 카운트하는 조건 추가
          "  AND udm.completedAt BETWEEN :startDate AND :endDate "
          + // 2. createdAt -> completedAt 으로 변경
          "GROUP BY DAY(udm.completedAt)")
  List<MissionCountPerDay> findMissionCountsPerDay(
      @Param("user") User user,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);
}
