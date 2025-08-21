package com.example.cp_main_be.domain.mission.quiz.domain.repository;

import com.example.cp_main_be.domain.mission.quiz.domain.Quiz;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

  Optional<Quiz> findByDailyMissionMasters_Id(Long missionMasterId);
}
