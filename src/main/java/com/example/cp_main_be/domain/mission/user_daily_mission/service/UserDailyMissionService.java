package com.example.cp_main_be.domain.mission.user_daily_mission.service;

import com.example.cp_main_be.domain.garden.image.DailyMissionImage;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.repository.DailyMissionMasterRepository;
import com.example.cp_main_be.domain.mission.daily_mission_master.dto.response.DailyMissionResponseDTO;
import com.example.cp_main_be.domain.mission.quiz.domain.Quiz;
import com.example.cp_main_be.domain.mission.quiz.domain.QuizOptions;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizOptionsRepository;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizRepository;
import com.example.cp_main_be.domain.mission.quiz.dto.QuizRequestDTO;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserDailyMission;
import com.example.cp_main_be.domain.mission.user_daily_mission.repository.UserDailyMissionRepository;
import com.example.cp_main_be.global.infra.S3Uploader;
import java.time.LocalDateTime;
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
  private final DailyMissionMasterRepository dailyMissionMasterRepository;
  private final S3Uploader s3Uploader;
  private final QuizOptionsRepository quizOptionsRepository;
  private final QuizRepository quizRepository;

  public DailyMissionResponseDTO getDailyMissions(Long userId) {
    List<UserDailyMission> dailyMissions = userDailyMissionRepository.findAllByUser_Id(userId);

    List<DailyMissionMaster> dailyMissionMasters =
        dailyMissions.stream().map(UserDailyMission::getDailyMissionMaster).toList();

    return DailyMissionResponseDTO.from(dailyMissionMasters);
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
            .findByDailyMissionMaster_Id(userDailyMission.getDailyMissionMaster().getId())
            .orElseThrow(() -> new RuntimeException("퀴즈가 존재하지 않습니다."));

    List<QuizOptions> quizOptionsList = quizOptionsRepository.findAllByQuizId(quiz.getId());

    // 사용자가 선택한 옵션 찾기
    QuizOptions selectedOption = null;
    boolean isCorrect = false;

    for (QuizOptions quizOptions : quizOptionsList) {
      if (quizOptions.getOptionOrder() == request.getAnswerNumber()) {
        selectedOption = quizOptions;
        isCorrect = quizOptions.isCorrect();
        break;
      }
    }

    if (selectedOption == null) {
      throw new RuntimeException("선택한 답안이 유효하지 않습니다.");
    }

    // UserDailyMission에 답안 정보 저장 (엔티티 필드에 맞춰서)
    userDailyMission.setSelectedOptionId(selectedOption.getId());
    userDailyMission.setSelectedAnswerNumber(selectedOption.getOptionOrder());
    userDailyMission.setIsQuizCorrect(isCorrect);
    userDailyMission.setQuizAnsweredAt(LocalDateTime.now());

    // 정답이면 미션 완료 처리
    if (isCorrect) {
      userDailyMission.setCompleted(true); // isCompleted -> setCompleted
      userDailyMission.setCompletedAt(LocalDateTime.now()); // 완료 시간 설정
      // 점수 부여 로직 (필요에 따라)
      userDailyMission.setScore(10L); // 예시 점수
    }

    userDailyMissionRepository.save(userDailyMission);

    return isCorrect;
  }

  // 미션 완료 처리
  public void completeDailyMission(Long userDailyMissionId) {
    UserDailyMission userDailyMission = getUserDailyMission(userDailyMissionId);
    userDailyMission.setCompleted(true);
    userDailyMission.setCompletedAt(LocalDateTime.now());
    userDailyMissionRepository.save(userDailyMission);
  }

  // 공통 메서드
  private UserDailyMission getUserDailyMission(Long userDailyMissionId) {
    return userDailyMissionRepository
        .findById(userDailyMissionId)
        .orElseThrow(() -> new RuntimeException("해당 ID를 갖는 미션이 존재하지 않습니다."));
  }

  private Quiz getQuizByMissionId(Long missionId) {
    return quizRepository
        .findByDailyMissionMaster_Id(missionId)
        .orElseThrow(() -> new RuntimeException("퀴즈가 존재하지 않습니다."));
  }
}
