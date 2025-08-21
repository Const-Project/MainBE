package com.example.cp_main_be.domain.member.auth.presentation;

import com.example.cp_main_be.domain.member.auth.dto.response.TokenRefreshResponse;
import com.example.cp_main_be.domain.member.auth.service.AuthService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "인증 API", description = "인증 관련 기능을 제공합니다.")
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "액세스 토큰 재발급", description = "리프레시 토큰으로 새로운 액세스/리프레시 토큰을 발급(롤링)합니다.")
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshAccessToken(
      @RequestHeader("X-Refresh-Token") String refreshToken,
      @RequestHeader(value = "X-Client-Device-Id", required = false) String deviceId) {
    TokenRefreshResponse response = authService.refreshAccessToken(refreshToken, deviceId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "신규 익명 계정 등록", description = "첫 방문자를 위해 새로운 익명 계정을 생성하고 토큰을 발급합니다.")
  @PostMapping("/register-anonymous")
  public ResponseEntity<ApiResponse<TokenRefreshResponse>> registerAnonymous(
      @RequestHeader(value = "X-Client-Device-Id", required = false) String deviceId) {
    TokenRefreshResponse tokens = authService.registerNewAnonymousUser(deviceId);
    return ResponseEntity.ok(ApiResponse.success(tokens));
  }
}
