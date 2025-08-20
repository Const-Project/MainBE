package com.example.cp_main_be.domain.mission.user_daily_mission.repository;

import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserDailyMission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDailyMissionRepository extends JpaRepository<UserDailyMission, Long> {
  List<UserDailyMission> findAllByUserId(Long userId);
}
