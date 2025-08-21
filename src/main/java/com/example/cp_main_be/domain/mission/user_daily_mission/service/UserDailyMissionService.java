package com.example.cp_main_be.domain.mission.user_daily_mission.service;

import com.example.cp_main_be.domain.content.image.DailyMissionImage;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.repository.DailyMissionMastersRepository;
import com.example.cp_main_be.domain.mission.daily_mission_master.dto.response.DailyMissionResponseDTO;
import com.example.cp_main_be.domain.mission.quiz.domain.Quiz;
import com.example.cp_main_be.domain.mission.quiz.domain.QuizOptions;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizOptionsRepository;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizRepository;
import com.example.cp_main_be.domain.mission.quiz.dto.QuizRequestDTO;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserDailyMission;
import com.example.cp_main_be.domain.mission.user_daily_mission.repository.UserDailyMissionRepository;
import com.example.cp_main_be.global.infra.S3Uploader;
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
    List<UserDailyMission> dailyMissions = userDailyMissionRepository.findAllByUserId(userId);

    List<DailyMissionMaster> dailyMissionMasters =
        dailyMissions.stream().map(UserDailyMission::getDailyMissionMaster).toList();

    return DailyMissionResponseDTO.from(dailyMissionMasters);
  }

  public void completeDailyMission(Long dailyMissionId) {
    UserDailyMission mission = userDailyMissionRepository.findById(dailyMissionId).orElse(null);
    if (mission == null) throw new RuntimeException("미션을 찾을 수 없습니다.");
    userDailyMissionRepository.delete(mission);
  }

  public String uploadPictureForDailyMission(Long userDailyMissionId, MultipartFile file) {
    UserDailyMission userDailyMission =
        userDailyMissionRepository
            .findById(userDailyMissionId)
            .orElseThrow(() -> new RuntimeException("미션을 찾을 수 없습니다."));

    String imageUrl = s3Uploader.upload(file, "mission-images");

    userDailyMission.setDailyMissionImage(DailyMissionImage.builder().imageUrl(imageUrl).build());

    if (userDailyMission.getDailyMissionImage().getImageUrl().equals(imageUrl)) {
      userDailyMission.setCompleted(true);
    }

    return imageUrl;
  }

  public Boolean summitAnswer(QuizRequestDTO request, Long userDailyMissionId) {
    UserDailyMission userDailyMission =
        userDailyMissionRepository
            .findById(userDailyMissionId)
            .orElseThrow(() -> new RuntimeException("미션을 찾을 수 없습니다."));
    Quiz quiz =
        quizRepository
            .findByDailyMissionMasters_Id(userDailyMission.getDailyMissionMaster().getId())
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
