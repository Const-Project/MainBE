package com.example.cp_main_be.domain.garden.wateringlog.domain.repository;

import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.wateringlog.domain.FriendWateringLog;
import com.example.cp_main_be.domain.member.user.domain.User;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendWateringLogRepository extends JpaRepository<FriendWateringLog, Long> {

  int countByWaterGiverAndWateredAtAfter(User waterGiver, LocalDateTime startOfDay);

  boolean existsByWaterGiverAndWateredGardenAndWateredAtAfter(
      User waterGiver, Garden wateredGarden, LocalDateTime startOfDay);
}
