package com.example.cp_main_be.domain.policy.presentation;

import com.example.cp_main_be.domain.policy.service.PolicyService;
import com.example.cp_main_be.global.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/policy")
@Tag(name = "약관 API", description = "이용약관 및 정책 관련 기능을 제공합니다.")
public class PolicyController {

  private final PolicyService policyService;

  @Operation(summary = "약관 조회", description = "이용약관을 조회합니다")
  @GetMapping
  public ResponseEntity<ApiResponse<String>> getPolicy() {
    String policyContent = policyService.getPolicyContent();
    return ResponseEntity.ok(ApiResponse.success(policyContent));
  }
}
