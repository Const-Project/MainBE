package com.example.cp_main_be.domain.member.auth.presentation;

import com.example.cp_main_be.domain.member.auth.dto.response.TokenRefreshResponse;
import com.example.cp_main_be.domain.member.auth.service.AuthService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "인증 API", description = "인증 관련 기능을 제공합니다.")
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "액세스 토큰 재발급", description = "리프레시 토큰으로 새로운 액세스 토큰을 발급받습니다.")
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshAccessToken(
      // 1. "Authorization" 헤더 대신 커스텀 헤더 "X-Refresh-Token" 사용
      @RequestHeader("X-Refresh-Token") String refreshToken) {

    // 2. try-catch 블록 제거. 실패 시 예외가 발생하여 GlobalExceptionHandler가 처리하도록 함.
    TokenRefreshResponse response = authService.refreshAccessToken(refreshToken);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "신규 익명 계정 등록", description = "첫 방문자를 위해 새로운 익명 계정을 생성하고 토큰을 발급합니다.")
  @PostMapping("/register-anonymous")
  public ResponseEntity<ApiResponse<TokenRefreshResponse>> registerAnonymous() {
    TokenRefreshResponse tokens = authService.registerNewAnonymousUser();
    return ResponseEntity.ok(ApiResponse.success(tokens));
  }
}
