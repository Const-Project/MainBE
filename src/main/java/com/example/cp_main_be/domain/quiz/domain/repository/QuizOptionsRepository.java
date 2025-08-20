package com.example.cp_main_be.domain.quiz.domain.repository;

import com.example.cp_main_be.domain.quiz.domain.QuizOptions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizOptionsRepository extends JpaRepository<QuizOptions, Long> {
    List<QuizOptions> findAllByQuizId (Long quizId);
}
