package com.example.cp_main_be.domain.reports.domain.repository;

import com.example.cp_main_be.domain.reports.domain.Reports;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Reports, Long> {
}
