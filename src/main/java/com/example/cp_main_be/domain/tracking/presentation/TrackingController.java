package com.example.cp_main_be.domain.tracking.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.tracking.dto.TrackingReportResponse;
import com.example.cp_main_be.domain.tracking.service.TrackingService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
@Tag(name = "2주 트래킹 API", description = "사용자의 2주간 마음 상태 트래킹을 제공합니다.")
public class TrackingController {

  private final TrackingService trackingService;

  @Operation(summary = "트래킹 보고서 조회", description = "지난 14일간의 설문 결과와 활동을 분석하여 리포트를 제공합니다.")
  @GetMapping("/report")
  public ResponseEntity<ApiResponse<TrackingReportResponse>> getTrackingReport(
      @AuthenticationPrincipal User user) {
    TrackingReportResponse response = trackingService.getTrackingReport(user.getId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
