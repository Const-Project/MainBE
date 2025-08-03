package com.example.cp_main_be.domain.auth.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.cp_main_be.domain.auth.dto.response.TokenRefreshResponse;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.jwt.JwtTokenProvider;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private UserRepository userRepository;

  @InjectMocks private AuthService authService;

  @DisplayName("액세스 토큰 재발급 성공")
  @Test
  void refreshAccessToken_success() {
    // given
    String refreshToken = "validRefreshToken";
    String userUuid = UUID.randomUUID().toString();
    User user = User.builder().uuid(UUID.fromString(userUuid)).build();
    String newAccessToken = "newAccessToken";
    String newRefreshToken = "newRefreshToken"; // 새로운 refresh token 추가

    given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
    given(jwtTokenProvider.getUuidFromToken(refreshToken)).willReturn(userUuid);
    given(userRepository.findByUuid(UUID.fromString(userUuid))).willReturn(Optional.of(user));
    given(jwtTokenProvider.generateAccessToken(userUuid)).willReturn(newAccessToken);
    given(jwtTokenProvider.generateRefreshToken(userUuid)).willReturn(newRefreshToken); // 추가

    // when
    TokenRefreshResponse response = authService.refreshAccessToken(refreshToken);

    // then
    Assertions.assertNotNull(response);
    Assertions.assertEquals(newAccessToken, response.getAccessToken());
    Assertions.assertEquals(newRefreshToken, response.getRefreshToken()); // 새로운 토큰 검증
    Assertions.assertFalse(response.isNewAccount());

    verify(jwtTokenProvider).validateToken(refreshToken);
    verify(jwtTokenProvider).getUuidFromToken(refreshToken);
    verify(userRepository).findByUuid(UUID.fromString(userUuid));
    verify(jwtTokenProvider).generateAccessToken(userUuid);
    verify(jwtTokenProvider).generateRefreshToken(userUuid); // 추가 검증
  }

  @DisplayName("액세스 토큰 재발급 실패 - 유효하지 않은 Refresh Token")
  @Test
  void refreshAccessToken_fail_invalidRefreshToken() {
    // given
    String refreshToken = "invalidRefreshToken";
    given(jwtTokenProvider.validateToken(refreshToken)).willReturn(false);

    // when & then
    Assertions.assertThrows(
        RuntimeException.class, () -> authService.refreshAccessToken(refreshToken));
    verify(jwtTokenProvider).validateToken(refreshToken);
    verify(jwtTokenProvider, org.mockito.Mockito.never()).getUuidFromToken(anyString());
  }

  @DisplayName("액세스 토큰 재발급 실패 - 사용자 없음")
  @Test
  void refreshAccessToken_fail_userNotFound() {
    // given
    String refreshToken = "validRefreshToken";
    String userUuid = UUID.randomUUID().toString();

    given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
    given(jwtTokenProvider.getUuidFromToken(refreshToken)).willReturn(userUuid);
    given(userRepository.findByUuid(UUID.fromString(userUuid))).willReturn(Optional.empty());

    // when & then
    Assertions.assertThrows(
        RuntimeException.class, () -> authService.refreshAccessToken(refreshToken));
    verify(jwtTokenProvider).validateToken(refreshToken);
    verify(jwtTokenProvider).getUuidFromToken(refreshToken);
    verify(userRepository).findByUuid(UUID.fromString(userUuid));
    verify(jwtTokenProvider, org.mockito.Mockito.never()).generateAccessToken(anyString());
  }
}
