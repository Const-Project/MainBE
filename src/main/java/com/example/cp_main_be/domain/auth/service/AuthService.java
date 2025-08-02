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

    // 새로운 Access Token과 Refresh Token 모두 발급
    String newAccessToken = jwtTokenProvider.generateAccessToken(uuid);
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(uuid);

    return new TokenRefreshResponse(newAccessToken, newRefreshToken);
  }

  public TokenRefreshResponse createNewAnonymousAccount() {
    // 새로운 UUID 생성하여 사용자 등록
    String newUuid = java.util.UUID.randomUUID().toString();
    // 닉네임에 UUID 일부를 추가하여 고유성 확보
    String newNickname = "익명의 사용자-" + newUuid.substring(0, 4);

    com.example.cp_main_be.domain.user.domain.User newUser =
        com.example.cp_main_be.domain.user.domain.User.builder()
            .uuid(java.util.UUID.fromString(newUuid))
            .username(newNickname)
            .build();

    userRepository.save(newUser);

    String accessToken = jwtTokenProvider.generateAccessToken(newUuid);
    String refreshToken = jwtTokenProvider.generateRefreshToken(newUuid);

    // 데이터 유실 경고 로그: 이 시점에서 이전 UUID와 연결된 데이터는 고립됩니다.
    log.warn("리프레시 토큰 만료로 인해 새로운 익명 계정을 생성했습니다. 이전 데이터는 더 이상 연결되지 않습니다. New UUID: {}", newUuid);
    return new TokenRefreshResponse(accessToken, refreshToken, true);
  }
}
