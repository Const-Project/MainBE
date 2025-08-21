package com.example.cp_main_be.domain.mission.user_daily_mission.repository;

import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserDailyMissions;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDailyMissionRepository extends JpaRepository<UserDailyMissions, Long> {
  List<UserDailyMissions> findAllByUserId(Long userId);
}
