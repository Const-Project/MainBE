package com.example.cp_main_be.domain.member.auth.domain.repository;

import com.example.cp_main_be.domain.member.auth.domain.RefreshToken;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
  Optional<RefreshToken> findByToken(String token);

  List<RefreshToken> findAllByUserUuid(UUID userUuid);

  List<RefreshToken> findAllByUserUuidAndDeviceId(UUID userUuid, String deviceId);

  void deleteByToken(String token);

  void deleteAllByUserUuid(UUID userUuid);

  void deleteAllByExpiresAtBefore(LocalDateTime now);
}
