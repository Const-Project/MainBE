package com.example.cp_main_be.domain.realquiz.repository;

import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import com.example.cp_main_be.domain.realquiz.RealQuiz;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealQuizRepostitory extends JpaRepository<RealQuiz, Long> {

  List<RealQuiz> findAllByQuizType(QuizType quizType);
}
