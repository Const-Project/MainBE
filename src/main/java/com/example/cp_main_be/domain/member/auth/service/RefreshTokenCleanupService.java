package com.example.cp_main_be.domain.member.auth.service;

import com.example.cp_main_be.domain.member.auth.domain.repository.RefreshTokenRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenCleanupService {

  private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupService.class);

  private final RefreshTokenRepository refreshTokenRepository;

  /** 매일 새벽 3시에 만료된 리프레시 토큰 정리 */
  @Scheduled(cron = "0 0 3 * * *")
  public void cleanupExpiredTokens() {
    int beforeCount = (int) refreshTokenRepository.count();
    refreshTokenRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
    int afterCount = (int) refreshTokenRepository.count();

    log.info(
        "✅ 만료된 Refresh Token 정리 완료: {} → {} ({}개 삭제)",
        beforeCount,
        afterCount,
        beforeCount - afterCount);
  }
}
