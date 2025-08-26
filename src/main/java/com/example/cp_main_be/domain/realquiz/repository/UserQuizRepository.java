package com.example.cp_main_be.domain.realquiz.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import com.example.cp_main_be.domain.realquiz.RealQuiz;
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
}
