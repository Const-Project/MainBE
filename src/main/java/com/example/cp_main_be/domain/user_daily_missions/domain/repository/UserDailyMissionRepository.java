package com.example.cp_main_be.domain.user_daily_missions.domain.repository;


import com.example.cp_main_be.domain.misson.domain.DailyMissionMasters;
import com.example.cp_main_be.domain.user_daily_missions.domain.UserDailyMissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDailyMissionRepository extends JpaRepository<UserDailyMissions, Long> {
    List<UserDailyMissions> findAllByUserId(Long userId);
}
