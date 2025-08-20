package com.example.cp_main_be.domain.mission.quiz.domain.repository;

import com.example.cp_main_be.domain.mission.quiz.domain.QuizOptions;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizOptionsRepository extends JpaRepository<QuizOptions, Long> {
  List<QuizOptions> findAllByQuizId(Long quizId);
}
