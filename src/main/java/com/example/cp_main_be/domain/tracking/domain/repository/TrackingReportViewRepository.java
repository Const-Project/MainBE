package com.example.cp_main_be.domain.tracking.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.tracking.domain.TrackingReportView;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrackingReportViewRepository extends JpaRepository<TrackingReportView, Long> {

  boolean existsByUserAndCycleKey(User user, String cycleKey);

  Optional<TrackingReportView> findByUserAndCycleKey(User user, String cycleKey);

  @Modifying
  @Query("DELETE FROM TrackingReportView trv WHERE trv.user = :user")
  void deleteAllByUser(@Param("user") User user);
}
