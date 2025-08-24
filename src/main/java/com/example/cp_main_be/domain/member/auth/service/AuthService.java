package com.example.cp_main_be.domain.member.auth.service;

import com.example.cp_main_be.domain.member.auth.domain.RefreshToken;
import com.example.cp_main_be.domain.member.auth.domain.repository.RefreshTokenRepository;
import com.example.cp_main_be.domain.member.auth.dto.request.RegistrationRequest;
import com.example.cp_main_be.domain.member.auth.dto.response.AnonymousRegistrationResponse;
import com.example.cp_main_be.domain.member.auth.dto.response.TokenRefreshResponse;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.jwt.JwtTokenProvider;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final Logger logger = LoggerFactory.getLogger(AuthService.class);

  /** 리프레시 토큰으로 액세스 토큰 재발급 + (권장) 리프레시 토큰 롤링 */
  public TokenRefreshResponse refreshAccessToken(String incomingRefreshToken, String deviceId) {
    // 1) 서명/만료 기본 검증
    if (!jwtTokenProvider.validateToken(incomingRefreshToken)) {
      throw new CustomApiException(ErrorCode.INVALID_TOKEN);
    }

    // 2) DB에 존재하는지 확인 (서버 관리 토큰이 아닌 경우 거절)
    RefreshToken saved =
        refreshTokenRepository
            .findByToken(incomingRefreshToken)
            .orElseThrow(() -> new CustomApiException(ErrorCode.INVALID_TOKEN));

    // 3) 서버 인지 만료도 체크 (DB 기록 기준)
    if (saved.getExpiresAt().isBefore(LocalDateTime.now())) {
      refreshTokenRepository.deleteByToken(incomingRefreshToken);
      throw new CustomApiException(ErrorCode.INVALID_TOKEN);
    }

    // 4) JWT에서 uuid 추출
    String uuidStr = jwtTokenProvider.getUuidFromToken(incomingRefreshToken);
    UUID uuid = UUID.fromString(uuidStr);

    // 5) 사용자 존재 확인 (안전망)
    User user =
        userRepository
            .findByUuid(uuid)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));

    // 6) 액세스 토큰 새로 발급
    String newAccessToken = jwtTokenProvider.generateAccessToken(uuid.toString());

    // === 선택 사항: 롤링 여부 설정 ===
    boolean rotateRefreshToken = true; // 필요 시 yml로 뺄 수 있음
    if (!rotateRefreshToken) {
      // 롤링 안 함 → 기존 리프레시 토큰 그대로 반환
      return new TokenRefreshResponse(newAccessToken, incomingRefreshToken, false);
    }

    // 7) 롤링: 기존 토큰 삭제 → 신규 토큰 발급/저장
    refreshTokenRepository.deleteByToken(incomingRefreshToken);

    String newRefreshToken = jwtTokenProvider.generateRefreshToken(uuid.toString());
    LocalDateTime newExpiry = jwtTokenProvider.getExpirationLocalDateTime(newRefreshToken);

    RefreshToken newRt =
        RefreshToken.builder()
            .token(newRefreshToken)
            .userUuid(uuid)
            .expiresAt(newExpiry)
            .deviceId(deviceId)
            .build();
    refreshTokenRepository.save(newRt);

    return new TokenRefreshResponse(newAccessToken, newRefreshToken, false);
  }

  // [수정] 신규 사용자 가입 메서드
  public AnonymousRegistrationResponse registerNewUser(
      RegistrationRequest request, String deviceId) {

    // 2. 랜덤 닉네임 생성 로직 삭제, 요청받은 닉네임 사용
    UUID newUuid = UUID.randomUUID();
    String nickname = request.getNickname();

    User newUser =
        User.builder()
            .uuid(newUuid)
            .nickname(nickname) // 사용자가 입력한 닉네임으로 설정
            .build();
    userRepository.save(newUser);

    String accessToken = jwtTokenProvider.generateAccessToken(newUuid.toString());
    String refreshToken = jwtTokenProvider.generateRefreshToken(newUuid.toString());
    LocalDateTime expiry = jwtTokenProvider.getExpirationLocalDateTime(refreshToken);

    RefreshToken rt =
        RefreshToken.builder()
            .token(refreshToken)
            .userUuid(newUuid)
            .expiresAt(expiry)
            .deviceId(deviceId)
            .build();
    refreshTokenRepository.save(rt);

    return AnonymousRegistrationResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .userId(newUser.getId())
        .nickname(nickname) // 생성된 닉네임 반환
        .isNewUser(true)
        .build();
  }

  /** 특정 리프레시 토큰 무효화(로그아웃) */
  public void revokeRefreshToken(String refreshToken) {
    refreshTokenRepository.deleteByToken(refreshToken);
  }

  /** 해당 유저의 전체 리프레시 토큰 무효화(강제 로그아웃 All) */
  public void revokeAllByUser(UUID userUuid) {
    refreshTokenRepository.deleteAllByUserUuid(userUuid);
  }

  /** 만료된 리프레시 토큰 청소 (스케쥴러로 주기적으로 호출) */
  public void purgeExpiredTokens() {
    refreshTokenRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
  }
}
