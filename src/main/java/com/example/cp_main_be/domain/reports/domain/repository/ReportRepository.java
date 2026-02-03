package com.example.cp_main_be.domain.reports.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.reports.domain.Reports;
import com.example.cp_main_be.domain.reports.enums.ReportStatus;
import com.example.cp_main_be.domain.reports.enums.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Reports, Long> {
  boolean existsByUserAndTargetTypeAndTargetIdAndStatus(
      User user, TargetType targetType, Long targetId, ReportStatus status);
}
