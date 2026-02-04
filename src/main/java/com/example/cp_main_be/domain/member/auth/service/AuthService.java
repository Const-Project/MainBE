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
import com.example.cp_main_be.global.supabase.SupabaseAuthClient;
import com.example.cp_main_be.global.supabase.SupabaseUserResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  private final SupabaseAuthClient supabaseAuthClient;

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
    if (deviceId != null && saved.getDeviceId() != null && !deviceId.equals(saved.getDeviceId())) {
      throw new CustomApiException(ErrorCode.INVALID_TOKEN);
    }
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

    User savedUser = saveAndInitializeUser(newUser);
    return issueTokens(savedUser, true, deviceId);
  }

  public AnonymousRegistrationResponse loginWithSupabase(String accessToken, String deviceId) {
    SupabaseUserResponse supabaseUser = fetchSupabaseUser(accessToken);
    return loginWithSupabaseTransactional(supabaseUser, deviceId);
  }

  @Transactional
  public AnonymousRegistrationResponse loginWithSupabaseTransactional(
      SupabaseUserResponse supabaseUser, String deviceId) {
    String oauthSubject = supabaseUser.getId();
    String oauthProvider = extractProvider(supabaseUser);
    if (oauthSubject == null || oauthProvider == null || oauthProvider.isBlank()) {
      throw new CustomApiException(ErrorCode.INVALID_REQUEST);
    }

    Optional<User> existing =
        userRepository.findByOauthProviderAndOauthSubject(oauthProvider, oauthSubject);

    if (existing.isPresent()) {
      User user = existing.get();
      boolean updated = applyProfileUpdates(user, supabaseUser);
      if (updated) {
        userRepository.save(user);
      }
      return issueTokens(user, false, deviceId);
    }

    String nickname = buildUniqueNickname(supabaseUser);
    String email = resolveEmail(supabaseUser);
    UUID newUuid = UUID.randomUUID();

    User newUser =
        User.builder()
            .uuid(newUuid)
            .nickname(nickname)
            .email(email)
            .profileImageUrl(extractProfileImageUrl(supabaseUser))
            .oauthProvider(oauthProvider)
            .oauthSubject(oauthSubject)
            .avatarList(new ArrayList<>())
            .diaries(new ArrayList<>())
            .gardens(new ArrayList<>())
            .build();

    User savedUser = saveAndInitializeUser(newUser);
    return issueTokens(savedUser, true, deviceId);
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

  private User saveAndInitializeUser(User newUser) {
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
    wishTreeService.addPointsToWishTree(savedUser.getId(), 0L);

    return savedUser;
  }

  private AnonymousRegistrationResponse issueTokens(User user, boolean isNewUser, String deviceId) {
    String accessToken = jwtTokenProvider.generateAccessToken(user.getUuid().toString());
    String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUuid().toString());
    LocalDateTime expiry = jwtTokenProvider.getExpirationLocalDateTime(refreshToken);

    RefreshToken rt =
        RefreshToken.builder()
            .token(refreshToken)
            .userUuid(user.getUuid())
            .expiresAt(expiry)
            .deviceId(deviceId)
            .build();
    refreshTokenRepository.save(rt);

    return AnonymousRegistrationResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .userId(user.getId())
        .nickname(user.getNickname())
        .isNewUser(isNewUser)
        .build();
  }

  private String extractProvider(SupabaseUserResponse supabaseUser) {
    Map<String, Object> appMetadata = supabaseUser.getAppMetadata();
    if (appMetadata == null) {
      return null;
    }
    Object provider = appMetadata.get("provider");
    if (provider instanceof String providerStr && !providerStr.isBlank()) {
      return providerStr;
    }
    Object providers = appMetadata.get("providers");
    if (providers instanceof List<?> providerList && !providerList.isEmpty()) {
      Object first = providerList.get(0);
      if (first instanceof String firstProvider && !firstProvider.isBlank()) {
        return firstProvider;
      }
    }
    return null;
  }

  private boolean applyProfileUpdates(User user, SupabaseUserResponse supabaseUser) {
    boolean updated = false;

    if (user.getEmail() == null && supabaseUser.getEmail() != null) {
      user.setEmail(supabaseUser.getEmail());
      updated = true;
    }

    String profileImageUrl = extractProfileImageUrl(supabaseUser);
    if (user.getProfileImageUrl() == null && profileImageUrl != null) {
      user.setProfileImageUrl(profileImageUrl);
      updated = true;
    }

    return updated;
  }

  private String extractProfileImageUrl(SupabaseUserResponse supabaseUser) {
    Map<String, Object> userMetadata = supabaseUser.getUserMetadata();
    if (userMetadata == null) {
      return null;
    }
    Object avatarUrl = userMetadata.get("avatar_url");
    if (avatarUrl instanceof String avatarStr && !avatarStr.isBlank()) {
      return avatarStr;
    }
    Object pictureUrl = userMetadata.get("picture");
    if (pictureUrl instanceof String pictureStr && !pictureStr.isBlank()) {
      return pictureStr;
    }
    return null;
  }

  private String buildUniqueNickname(SupabaseUserResponse supabaseUser) {
    String base = "user";
    Map<String, Object> userMetadata = supabaseUser.getUserMetadata();
    if (userMetadata != null) {
      base =
          firstNonBlank(
              userMetadata, "nickname", "name", "full_name", "preferred_username", "user_name");
    }

    if (base == null || base.isBlank()) {
      base = "user";
    }

    String sanitized = base.replaceAll("\\s+", "");
    sanitized = sanitized.replaceAll("[^a-zA-Z0-9._-]", "");
    if (sanitized.isBlank()) {
      sanitized = "user";
    }

    int baseMaxLength = 249;
    if (sanitized.length() > baseMaxLength) {
      sanitized = sanitized.substring(0, baseMaxLength);
    }

    String candidate = sanitized;
    int attempts = 0;
    while (userRepository.existsByNickname(candidate) && attempts < 5) {
      candidate = sanitized + randomSuffix();
      attempts++;
    }
    if (userRepository.existsByNickname(candidate)) {
      candidate = "user" + randomSuffix();
    }
    return candidate;
  }

  private String firstNonBlank(Map<String, Object> metadata, String... keys) {
    for (String key : keys) {
      Object value = metadata.get(key);
      if (value instanceof String str && !str.isBlank()) {
        return str;
      }
    }
    return null;
  }

  private String randomSuffix() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 6);
  }

  private SupabaseUserResponse fetchSupabaseUser(String accessToken) {
    return supabaseAuthClient.fetchUser(accessToken);
  }

  private String resolveEmail(SupabaseUserResponse supabaseUser) {
    String email = supabaseUser.getEmail();
    if (email == null || email.isBlank()) {
      throw new CustomApiException(ErrorCode.INVALID_REQUEST);
    }
    return email;
  }
}
