package com.example.cp_main_be.domain.tracking.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.tracking.domain.TrackingReportView;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingReportViewRepository extends JpaRepository<TrackingReportView, Long> {

  boolean existsByUserAndCycleKey(User user, String cycleKey);

  Optional<TrackingReportView> findByUserAndCycleKey(User user, String cycleKey);
}
