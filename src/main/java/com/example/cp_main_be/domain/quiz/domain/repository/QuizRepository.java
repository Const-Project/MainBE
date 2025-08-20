package com.example.cp_main_be.domain.quiz.domain.repository;

import com.example.cp_main_be.domain.quiz.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByDailyMissionMasters_Id(Long missionMasterId);
}
