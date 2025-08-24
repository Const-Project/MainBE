package com.example.cp_main_be.domain.mission.quiz.domain.repository;

import com.example.cp_main_be.domain.mission.quiz.domain.Quiz;
import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

  Optional<Quiz> findByDailyMissionMaster_Id(Long missionMasterId);

  List<Quiz> findAllByQuizType(QuizType quizType);
}
