package com.example.cp_main_be.domain.realquiz.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserDailyMission;
import com.example.cp_main_be.domain.realquiz.RealQuiz;

import java.time.LocalDateTime;
import java.util.List;

import com.example.cp_main_be.domain.realquiz.UserQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RealQuizRepostitory extends JpaRepository<RealQuiz, Long> {
  List<RealQuiz> findAllByQuizType(QuizType quizType);

}
