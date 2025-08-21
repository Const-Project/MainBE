package com.example.cp_main_be.domain.reports.domain.repository;

import com.example.cp_main_be.domain.reports.domain.ReportReason;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportReasonRepository extends JpaRepository<ReportReason, Long> {
  Optional<ReportReason> findByReasonText(String reason);
}
