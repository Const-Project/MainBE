package com.example.cp_main_be.domain.member.log.domain.repository;

import com.example.cp_main_be.domain.member.log.domain.UserDailyActivityLog;
import com.example.cp_main_be.domain.member.user.domain.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserDailyActivityLogRepository extends JpaRepository<UserDailyActivityLog, Long> {
  Optional<UserDailyActivityLog> findByUserAndDate(User user, LocalDate date);

  List<UserDailyActivityLog> findAllByUserAndDateBetween(
      User user, LocalDate startDate, LocalDate endDate);

  @Query(
      "SELECT COUNT(l) FROM UserDailyActivityLog l WHERE l.user = :user AND l.date BETWEEN :startDate AND :endDate AND l.hasWatered = true AND l.hasSunlight = true")
  long countPerfectDays(
      @Param("user") User user,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Query(
      "SELECT COUNT(DISTINCT l.user.id) FROM UserDailyActivityLog l WHERE l.date BETWEEN :startDate AND :endDate")
  long countDistinctActiveUserIds(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
