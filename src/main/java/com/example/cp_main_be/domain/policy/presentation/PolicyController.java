package com.example.cp_main_be.domain.policy.presentation;

import com.example.cp_main_be.domain.policy.service.PolicyService;
import com.example.cp_main_be.global.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/policy")
public class PolicyController {

  private final PolicyService policyService;

  @GetMapping
  public ResponseEntity<ApiResponse<String>> getPolicy() {
    String policyContent = policyService.getPolicyContent();
    return ResponseEntity.ok(ApiResponse.success(policyContent));
  }
}
