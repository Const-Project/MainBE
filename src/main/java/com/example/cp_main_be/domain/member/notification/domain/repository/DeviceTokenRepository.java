package com.example.cp_main_be.domain.member.notification.domain.repository;

import com.example.cp_main_be.domain.member.notification.domain.DeviceToken;
import com.example.cp_main_be.domain.member.user.domain.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
  Optional<DeviceToken> findByToken(String token);

  Optional<DeviceToken> findByUser(User user);

  @Query("select count(distinct dt.user.id) from DeviceToken dt")
  long countDistinctUserIds();

  @Query(
      "select count(distinct dt.user.id) from DeviceToken dt "
          + "where dt.user.lastAccessedAt between :startDate and :endDate")
  long countDistinctActiveUserIdsWithToken(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
