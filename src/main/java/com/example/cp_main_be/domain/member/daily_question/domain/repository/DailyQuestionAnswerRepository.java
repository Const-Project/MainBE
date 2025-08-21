package com.example.cp_main_be.domain.member.daily_question.domain.repository;

import com.example.cp_main_be.domain.member.daily_question.domain.DailyQuestionAnswer;
import com.example.cp_main_be.domain.member.user.domain.User;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyQuestionAnswerRepository extends JpaRepository<DailyQuestionAnswer, Long> {
  boolean existsByUserAndAnsweredDate(User user, LocalDate date);
}
