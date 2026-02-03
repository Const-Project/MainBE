package com.example.cp_main_be.domain.member.auth.service;

import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenRepository;
import com.example.cp_main_be.domain.member.auth.domain.RefreshToken;
import com.example.cp_main_be.domain.member.auth.domain.repository.RefreshTokenRepository;
import com.example.cp_main_be.domain.member.auth.dto.request.RegistrationRequest;
import com.example.cp_main_be.domain.member.auth.dto.response.AnonymousRegistrationResponse;
import com.example.cp_main_be.domain.member.auth.dto.response.TokenRefreshResponse;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.mission.wishTree.WishTreeService;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.jwt.JwtTokenProvider;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final Logger logger = LoggerFactory.getLogger(AuthService.class);
  private final WishTreeService wishTreeService;
  private final GardenRepository gardenRepository;

  /** 리프레시 토큰으로 액세스 토큰 재발급 + (권장) 리프레시 토큰 롤링 */
  @Transactional
  public TokenRefreshResponse refreshAccessToken(String incomingRefreshToken, String deviceId) {
    // 1) ~ 5) 까지의 검증 로직은 동일합니다.
    try {
      if (!jwtTokenProvider.validateToken(incomingRefreshToken)) {
        throw new CustomApiException(ErrorCode.INVALID_TOKEN);
      }
    } catch (Exception e) {
      throw new CustomApiException(ErrorCode.INVALID_TOKEN);
    }
    RefreshToken saved =
        refreshTokenRepository
            .findByToken(incomingRefreshToken)
            .orElseThrow(() -> new CustomApiException(ErrorCode.INVALID_TOKEN));
    if (saved.getExpiresAt().isBefore(LocalDateTime.now())) {
      refreshTokenRepository.deleteByToken(incomingRefreshToken);
      throw new CustomApiException(ErrorCode.INVALID_TOKEN);
    }
    String uuidStr = jwtTokenProvider.getUuidFromToken(incomingRefreshToken);
    UUID uuid = UUID.fromString(uuidStr);
    User user =
        userRepository
            .findByUuid(uuid)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));

    // 6) 새로운 Access Token만 발급합니다.
    String newAccessToken = jwtTokenProvider.generateAccessToken(uuid.toString());

    // 7) 롤링 로직을 모두 제거하고, 기존 Refresh Token을 그대로 반환합니다.
    return new TokenRefreshResponse(newAccessToken, incomingRefreshToken, false);
  }

  // [수정] 신규 사용자 가입 메서드
  @Transactional // 회원가입의 모든 과정을 하나의 트랜잭션으로 묶습니다.
  public AnonymousRegistrationResponse registerNewUser(
      RegistrationRequest request, String deviceId) {

    UUID newUuid = UUID.randomUUID();
    String nickname = request.getNickname();

    User newUser =
        User.builder()
            .uuid(newUuid)
            .nickname(nickname)
            .avatarList(new ArrayList<>())
            .diaries(new ArrayList<>())
            .gardens(new ArrayList<>())
            .build();

    // 1. 사용자를 먼저 저장합니다.
    User savedUser = userRepository.save(newUser);

    Garden firstGarden =
        Garden.builder().user(savedUser).slotNumber(1).isLocked(false).build(); // 1번은 기본 해금
    Garden secondGarden =
        Garden.builder().user(savedUser).slotNumber(2).isLocked(true).build(); // 2번은 잠김
    Garden thirdGarden =
        Garden.builder().user(savedUser).slotNumber(3).isLocked(true).build(); // 3번은 잠김
    Garden fourthGarden =
        Garden.builder().user(savedUser).slotNumber(4).isLocked(true).build(); // 4번은 잠김

    gardenRepository.saveAll(List.of(firstGarden, secondGarden, thirdGarden, fourthGarden));

    // 2. 위시트리 관련 로직을 수행합니다.
    // 만약 여기서 예외가 발생하면, 위에서 저장한 newUser까지 모두 롤백됩니다.
    wishTreeService.addPointsToWishTree(savedUser.getId(), 0L);

    // 3. 모든 것이 성공했을 때만 토큰을 생성하고 저장합니다.
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
        .userId(savedUser.getId()) // save() 후 반환된 객체의 ID 사용
        .nickname(nickname)
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
