package com.example.cp_main_be.domain.auth.presentation;

import com.example.cp_main_be.domain.auth.dto.response.TokenRefreshResponse;
import com.example.cp_main_be.domain.auth.service.AuthService;
import com.example.cp_main_be.global.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshAccessToken(
      @RequestHeader("Authorization") String refreshToken) {
    TokenRefreshResponse response = authService.refreshAccessToken(refreshToken);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
