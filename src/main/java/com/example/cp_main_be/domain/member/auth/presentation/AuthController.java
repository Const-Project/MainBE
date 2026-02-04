package com.example.cp_main_be.domain.member.auth.presentation;

import com.example.cp_main_be.domain.member.auth.dto.request.RegistrationRequest;
import com.example.cp_main_be.domain.member.auth.dto.request.SupabaseLoginRequest;
import com.example.cp_main_be.domain.member.auth.dto.response.AnonymousRegistrationResponse;
import com.example.cp_main_be.domain.member.auth.dto.response.TokenRefreshResponse;
import com.example.cp_main_be.domain.member.auth.service.AuthService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "인증 API", description = "인증 관련 기능을 제공합니다.")
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "액세스 토큰 재발급", description = "리프레시 토큰으로 새로운 액세스 토큰을 발급합니다.")
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshAccessToken(
      @RequestHeader("X-Refresh-Token") String refreshToken,
      @RequestHeader(value = "X-Client-Device-Id", required = false) String deviceId) {
    TokenRefreshResponse response = authService.refreshAccessToken(refreshToken, deviceId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  // [수정] 기존 registerAnonymous 메서드를 아래와 같이 변경
  @Operation(summary = "신규 계정 등록", description = "닉네임을 입력받아 새로운 계정을 생성하고 토큰과 닉네임을 발급합니다.")
  @PostMapping("/signup") // 엔드포인트 이름 변경
  public ResponseEntity<ApiResponse<AnonymousRegistrationResponse>> signup(
      @RequestBody @Valid RegistrationRequest request, // Request Body로 닉네임 받기
      @RequestHeader(value = "X-Client-Device-Id", required = false) String deviceId) {

    AnonymousRegistrationResponse response = authService.registerNewUser(request, deviceId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "Supabase 소셜 로그인", description = "Supabase OAuth 토큰을 우리 서비스 토큰으로 교환합니다.")
  @PostMapping("/supabase")
  public ResponseEntity<ApiResponse<AnonymousRegistrationResponse>> loginWithSupabase(
      @Valid @RequestBody SupabaseLoginRequest request,
      @RequestHeader(value = "X-Client-Device-Id", required = false) String deviceId) {

    AnonymousRegistrationResponse response =
        authService.loginWithSupabase(request.getAccessToken(), deviceId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
