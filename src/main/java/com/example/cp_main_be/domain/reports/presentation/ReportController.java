package com.example.cp_main_be.domain.reports.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.reports.dto.ReportRequestDto;
import com.example.cp_main_be.domain.reports.service.ReportService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "신고 API")
public class ReportController {

  private final ReportService reportService;

  @Operation(summary = "콘텐츠를 신고합니다")
  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createReport(
      @AuthenticationPrincipal User user, @RequestBody ReportRequestDto reportRequestDto) {
    reportService.createReport(user.getId(), reportRequestDto);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
