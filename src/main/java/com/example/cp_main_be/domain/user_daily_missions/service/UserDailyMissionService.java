package com.example.cp_main_be.domain.user_daily_missions.service;

import com.example.cp_main_be.domain.daily_mission_masters.domain.DailyMissionMasters;
import com.example.cp_main_be.domain.daily_mission_masters.domain.repository.DailyMissionMastersRepository;
import com.example.cp_main_be.domain.daily_mission_masters.dto.response.DailyMissionResponseDTO;
import com.example.cp_main_be.domain.image.DailyMissionImage;
import com.example.cp_main_be.domain.infra.s3.S3Uploader;
import com.example.cp_main_be.domain.quiz.domain.Quiz;
import com.example.cp_main_be.domain.quiz.domain.QuizOptions;
import com.example.cp_main_be.domain.quiz.domain.repository.QuizOptionsRepository;
import com.example.cp_main_be.domain.quiz.domain.repository.QuizRepository;
import com.example.cp_main_be.domain.quiz.dto.QuizRequestDTO;
import com.example.cp_main_be.domain.user_daily_missions.domain.UserDailyMissions;
import com.example.cp_main_be.domain.user_daily_missions.repository.UserDailyMissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserDailyMissionService {

  private final UserDailyMissionRepository userDailyMissionRepository;
  private final DailyMissionMastersRepository dailyMissionMastersRepository;
  private final S3Uploader s3Uploader;
  private final QuizOptionsRepository quizOptionsRepository;
  private final QuizRepository quizRepository;

  public DailyMissionResponseDTO getDailyMissions(Long userId) {
    List<UserDailyMissions> dailyMissions = userDailyMissionRepository.findAllByUserId(userId);

    List<DailyMissionMasters> dailyMissionMasters =
        dailyMissions.stream().map(UserDailyMissions::getDailyMissionMasters).toList();

    return DailyMissionResponseDTO.from(dailyMissionMasters);
  }

  public void completeDailyMission(Long dailyMissionId) {
    UserDailyMissions mission = userDailyMissionRepository.findById(dailyMissionId).orElse(null);
    if (mission == null) throw new RuntimeException("미션을 찾을 수 없습니다.");
    userDailyMissionRepository.delete(mission);
  }

  public String uploadPictureForDailyMission(Long userDailyMissionId, MultipartFile file) {
    UserDailyMissions userDailyMissions =
        userDailyMissionRepository
            .findById(userDailyMissionId)
            .orElseThrow(() -> new RuntimeException("미션을 찾을 수 없습니다."));

    String imageUrl = s3Uploader.upload(file, "mission-images");

    userDailyMissions.setDailyMissionImage(DailyMissionImage.builder().imageUrl(imageUrl).build());

    if (userDailyMissions.getDailyMissionImage().getImageUrl().equals(imageUrl)) {
      userDailyMissions.setCompleted(true);
    }

    return imageUrl;
  }

  public Boolean summitAnswer(QuizRequestDTO request, Long userDailyMissionId) {
    UserDailyMissions userDailyMissions =
        userDailyMissionRepository
            .findById(userDailyMissionId)
            .orElseThrow(() -> new RuntimeException("미션을 찾을 수 없습니다."));
    Quiz quiz =
        quizRepository
            .findByDailyMissionMasters_Id(userDailyMissions.getDailyMissionMasters().getId())
            .orElseThrow(() -> new RuntimeException("퀴즈가 존재하지 않습니다."));
    List<QuizOptions> quizOptionsList = quizOptionsRepository.findAllByQuizId(quiz.getId());
    for (QuizOptions quizOptions : quizOptionsList) {
      if (quizOptions.getOptionOrder() == request.getAnswerNumber()) {
        return quizOptions.isCorrect();
      }
    }
    return false;
  }
}
