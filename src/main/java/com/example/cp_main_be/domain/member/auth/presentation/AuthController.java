package com.example.cp_main_be.domain.member.auth.presentation;

import com.example.cp_main_be.domain.member.auth.dto.response.TokenRefreshResponse;
import com.example.cp_main_be.domain.member.auth.service.AuthService;
import com.example.cp_main_be.global.util.ApiResponse;
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

  @Operation(summary = "액세스 토큰 재발급", description = "액세스 토큰을 재발급합니다. 리프레시 토큰이 유효해야합니다.")
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshAccessToken(
      @RequestHeader("Authorization") String refreshToken) {
    try {
      TokenRefreshResponse response = authService.refreshAccessToken(refreshToken.substring(7));
      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (RuntimeException e) {
      // 리프레시 토큰 만료 시, 새로운 익명 계정 생성
      TokenRefreshResponse response = authService.createNewAnonymousAccount();
      return ResponseEntity.ok(ApiResponse.success(response));
    }
  }
}
