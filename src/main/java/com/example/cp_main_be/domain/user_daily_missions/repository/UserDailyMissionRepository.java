package com.example.cp_main_be.domain.user_daily_missions.repository;

import com.example.cp_main_be.domain.user_daily_missions.domain.UserDailyMissions;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDailyMissionRepository extends JpaRepository<UserDailyMissions, Long> {
  List<UserDailyMissions> findAllByUserId(Long userId);
}
