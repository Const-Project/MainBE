package com.example.cp_main_be.domain.reports.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.reports.domain.Reports;
import com.example.cp_main_be.domain.reports.enums.ReportStatus;
import com.example.cp_main_be.domain.reports.enums.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Reports, Long> {
  boolean existsByUserAndTargetTypeAndTargetIdAndStatus(
      User user, TargetType targetType, Long targetId, ReportStatus status);

  @Modifying
  @Query("DELETE FROM Reports r WHERE r.user = :user")
  void deleteAllByUser(@Param("user") User user);
}
