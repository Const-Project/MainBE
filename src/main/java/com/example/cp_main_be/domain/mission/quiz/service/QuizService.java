package com.example.cp_main_be.domain.mission.quiz.service;

import com.example.cp_main_be.domain.mission.daily_mission_masters.domain.DailyMissionMasters;
import com.example.cp_main_be.domain.mission.daily_mission_masters.domain.repository.DailyMissionMastersRepository;
import com.example.cp_main_be.domain.mission.quiz.domain.Quiz;
import com.example.cp_main_be.domain.mission.quiz.domain.QuizOptions;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizOptionsRepository;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizRepository;
import com.example.cp_main_be.domain.mission.quiz.dto.QuizResponseDTO;
import com.example.cp_main_be.domain.mission.user_daily_missions.domain.UserDailyMissions;
import com.example.cp_main_be.domain.mission.user_daily_missions.repository.UserDailyMissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

  private final UserDailyMissionRepository userDailyMissionRepository;
  private final DailyMissionMastersRepository dailyMissionMastersRepository;
  private final QuizRepository quizRepository;
  private final QuizOptionsRepository quizOptionsRepository;

  public QuizResponseDTO getQuiz(Long userDailyMissionId) {

    UserDailyMissions userDailyMissions =
        userDailyMissionRepository
            .findById(userDailyMissionId)
            .orElseThrow(() -> new RuntimeException("해당 ID를 갖는 미션이 존재하지 않습니다."));

    DailyMissionMasters dailyMissionMasters = userDailyMissions.getDailyMissionMasters();

    Quiz quiz =
        quizRepository
            .findByDailyMissionMasters_Id(dailyMissionMasters.getId())
            .orElseThrow(() -> new RuntimeException("퀴즈가 존재하지 않습니다."));

    List<QuizOptions> quizOptions = quizOptionsRepository.findAllByQuizId(quiz.getId());

    return QuizResponseDTO.builder()
        .quizType(quiz.getQuizType())
        .quizQuestion(quiz.getQuizQuestion())
        .quizOptions(quizOptions)
        .missionId(dailyMissionMasters.getId())
        .build();
  }
}
