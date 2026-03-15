package com.example.cp_main_be.domain.tracking.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.tracking.dto.TrackingPromptConfirmRequest;
import com.example.cp_main_be.domain.tracking.dto.TrackingPromptStatusResponse;
import com.example.cp_main_be.domain.tracking.dto.TrackingReportResponse;
import com.example.cp_main_be.domain.tracking.service.TrackingService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

  @Operation(summary = "홈 트래킹 모달 상태 조회", description = "홈에서 2주 리포트 확인 모달을 자동 노출할지 서버 기준으로 판정합니다.")
  @GetMapping("/report/status")
  public ResponseEntity<ApiResponse<TrackingPromptStatusResponse>> getTrackingPromptStatus(
      @AuthenticationPrincipal User user) {
    // 한글 주석:
    // MainAPP 은 eligible 값만 신뢰하므로, 주기 계산과 확인 여부 판단을 모두 서버에서 끝낸다.
    TrackingPromptStatusResponse response = trackingService.getTrackingPromptStatus(user.getId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "홈 트래킹 모달 확인 처리", description = "사용자가 현재 주기의 2주 리포트를 확인했음을 저장합니다.")
  @PostMapping("/report/confirm")
  public ResponseEntity<ApiResponse<Void>> confirmTrackingPrompt(
      @AuthenticationPrincipal User user, @RequestBody TrackingPromptConfirmRequest request) {
    // 한글 주석:
    // 닫기와 CTA 양쪽에서 같은 API 를 호출하므로, 서비스에서 idempotent 하게 처리한다.
    trackingService.confirmTrackingPrompt(user.getId(), request.getCycleKey());
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
