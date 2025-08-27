package com.example.cp_main_be.domain.realquiz.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.user_daily_mission.dto.MissionCountPerDay;
import com.example.cp_main_be.domain.realquiz.UserQuiz;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserQuizRepository extends JpaRepository<UserQuiz, Long> {
  Optional<UserQuiz> findByUser(User user);

  @Query(
      "SELECT uq FROM UserQuiz uq "
          + "WHERE uq.user = :user "
          + "AND uq.createdAt BETWEEN :startDate AND :endDate")
  List<UserQuiz> findAllTodayUserQuizByUser(
      @Param("user") User user,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT new com.example.cp_main_be.domain.mission.user_daily_mission.dto.MissionCountPerDay(DAY(uq.createdAt), COUNT(uq.id)) "
          + "FROM UserQuiz uq "
          + "WHERE uq.user = :user "
          + "  AND uq.isCompleted = true " // 1. 완료된 퀴즈만 필터링
          + "  AND uq.createdAt BETWEEN :startDate AND :endDate " // 2. 생성일을 기준으로 날짜 범위 필터링
          + "GROUP BY DAY(uq.createdAt)")
  List<MissionCountPerDay> findCompletedCountsPerDay(
      @Param("user") User user,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);
}
