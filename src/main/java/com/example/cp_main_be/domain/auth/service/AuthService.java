package com.example.cp_main_be.domain.auth.service;

import com.example.cp_main_be.domain.auth.dto.response.TokenRefreshResponse;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  public TokenRefreshResponse refreshAccessToken(String refreshToken) {
    // Refresh Token 유효성 검사
    if (!jwtTokenProvider.validateToken(refreshToken)) {
      throw new RuntimeException("Invalid Refresh Token"); // TODO: Custom Exception
    }

    String uuid = jwtTokenProvider.getUuidFromToken(refreshToken);

    // 사용자 존재 여부 확인 (선택 사항, Refresh Token이 유효하면 사용자도 유효하다고 가정할 수 있음)
    userRepository
        .findByUuid(java.util.UUID.fromString(uuid))
        .orElseThrow(() -> new RuntimeException("User not found")); // TODO: Custom Exception

    // 새로운 Access Token 발급
    String newAccessToken = jwtTokenProvider.generateAccessToken(uuid);

    return new TokenRefreshResponse(newAccessToken, refreshToken);
  }
}
