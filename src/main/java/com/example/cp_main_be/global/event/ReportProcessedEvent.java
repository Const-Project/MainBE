package com.example.cp_main_be.global.event;

import com.example.cp_main_be.domain.reports.domain.Reports;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReportProcessedEvent {
  private final Reports report;
}
