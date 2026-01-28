package com.example.cp_main_be.domain.member.daily_question.domain.repository;

import com.example.cp_main_be.domain.member.daily_question.domain.DailyQuestionAnswer;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.user_daily_mission.dto.MissionCountPerDay;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyQuestionAnswerRepository extends JpaRepository<DailyQuestionAnswer, Long> {
  boolean existsByUserAndAnsweredDate(User user, LocalDate date);

  @Query(
      "SELECT new com.example.cp_main_be.domain.mission.user_daily_mission.dto.MissionCountPerDay(DAY(dqa.answeredDate), COUNT(dqa.id)) "
          + "FROM DailyQuestionAnswer dqa "
          + "WHERE dqa.user = :user "
          + "  AND dqa.answeredDate BETWEEN CAST(:startDate AS date) AND CAST(:endDate AS date) "
          + "GROUP BY DAY(dqa.answeredDate)")
  List<MissionCountPerDay> findCompletedCountsPerDay(
      @Param("user") User user,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  List<DailyQuestionAnswer> findAllByUserAndAnsweredDateBetween(
      User user, LocalDate startDate, LocalDate endDate);
}
