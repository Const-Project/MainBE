package com.example.cp_main_be.domain.member.notification.domain.repository;

import com.example.cp_main_be.domain.member.notification.domain.DeviceToken;
import com.example.cp_main_be.domain.member.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
  Optional<DeviceToken> findByToken(String token);

  Optional<DeviceToken> findByUser(User user);
}
