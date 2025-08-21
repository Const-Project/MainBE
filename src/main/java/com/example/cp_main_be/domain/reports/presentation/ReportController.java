package com.example.cp_main_be.domain.reports.presentation;

import com.example.cp_main_be.domain.reports.dto.ReportRequestDto;
import com.example.cp_main_be.domain.reports.service.ReportService;
import com.example.cp_main_be.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createReport(
      @AuthenticationPrincipal Long userId, // Assuming user ID is available from security context
      @RequestBody ReportRequestDto reportRequestDto) {
    reportService.createReport(userId, reportRequestDto);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
