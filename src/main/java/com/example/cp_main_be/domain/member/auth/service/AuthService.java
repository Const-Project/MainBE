package com.example.cp_main_be.domain.member.auth.service;

import com.example.cp_main_be.domain.member.auth.dto.response.TokenRefreshResponse;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.jwt.JwtTokenProvider;
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
  private final Logger logger = LoggerFactory.getLogger(AuthService.class);

  public TokenRefreshResponse refreshAccessToken(String refreshToken) {
    if (!jwtTokenProvider.validateToken(refreshToken)) {
      // 명확한 예외를 던져 클라이언트가 재로그인(닉네임 입력)하도록 유도
      throw new CustomApiException(ErrorCode.INVALID_TOKEN);
    }

    String uuid = jwtTokenProvider.getUuidFromToken(refreshToken);

    // [수정] 리프레시 토큰은 만료 기간이 길기 때문에 보통 새로 발급하지 않거나,
    // 클라이언트와 협의하여 선택적으로 재발급(Rotation)할 수 있습니다. 여기서는 Access Token만 재발급합니다.
    String newAccessToken = jwtTokenProvider.generateAccessToken(uuid);

    return new TokenRefreshResponse(newAccessToken, refreshToken, false); // 기존 리프레시 토큰 반환
  }

  public TokenRefreshResponse registerNewAnonymousUser() {
    // 새로운 UUID와 기본 닉네임으로 사용자 생성
    UUID newUuid = UUID.randomUUID();
    String newNickname = "익명의 새싹-" + newUuid.toString().substring(0, 4);

    User newUser = User.builder().uuid(newUuid).username(newNickname).build();
    userRepository.save(newUser);

    // 새 사용자를 위한 토큰 발급
    String accessToken = jwtTokenProvider.generateAccessToken(newUuid.toString());
    String refreshToken = jwtTokenProvider.generateRefreshToken(newUuid.toString());

    return new TokenRefreshResponse(accessToken, refreshToken, true); // isNewUser 플래그 추가 가능
  }
}
