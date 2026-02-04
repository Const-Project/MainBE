package com.example.cp_main_be.domain.mission.user_daily_mission.service;

import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.domain.mission.daily_mission_master.MissionType;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import com.example.cp_main_be.domain.mission.daily_mission_master.dto.response.DailyMissionResponseDTO;
import com.example.cp_main_be.domain.mission.quiz.domain.Quiz;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizOptionsRepository;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizRepository;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserDailyMission;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.repository.UserDailyMissionRepository;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
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
  private final S3Uploader s3Uploader;
  private final QuizOptionsRepository quizOptionsRepository;
  private final QuizRepository quizRepository;
  private final UserService userService;

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
            .orElseThrow(() -> new CustomApiException(ErrorCode.MISSION_NOT_FOUND));

    String imageUrl = s3Uploader.upload(file, "mission-images");

    userDailyMission.setSubmissionImageUrl(imageUrl);
    userDailyMission.setCompleted(true);

    return imageUrl;
  }

  // 미션 완료 처리
  public void completeDailyMission(Long userDailyMissionId) {
    UserDailyMission userDailyMission = getUserDailyMission(userDailyMissionId);
    userDailyMission.setCompleted(true);
    userDailyMission.setCompletedAt(LocalDateTime.now());
    final int PHOTO_MISSION_COMPLETE_POINT = 15;
    final int QUIZ_MISSION_COMPLETE_POINT = 15;
    final int DIARY_MISSION_COMPLETE_POINT = 15;
    MissionType type = userDailyMission.getDailyMissionMaster().getMissionType();
    if (type.equals(MissionType.PHOTO))
      userService.addExperience(
          userService.getCurrentUser().getId(), (long) PHOTO_MISSION_COMPLETE_POINT);
    else if (type.equals(MissionType.QUIZ))
      userService.addExperience(
          userService.getCurrentUser().getId(), (long) QUIZ_MISSION_COMPLETE_POINT);
    else
      userService.addExperience(
          userService.getCurrentUser().getId(), (long) DIARY_MISSION_COMPLETE_POINT);
    userDailyMissionRepository.save(userDailyMission);
  }

  // 공통 메서드
  private UserDailyMission getUserDailyMission(Long userDailyMissionId) {
    return userDailyMissionRepository
        .findById(userDailyMissionId)
        .orElseThrow(() -> new CustomApiException(ErrorCode.MISSION_NOT_FOUND));
  }

  private Quiz getQuizByMissionId(Long missionId) {
    return quizRepository
        .findByDailyMissionMaster_Id(missionId)
        .orElseThrow(() -> new CustomApiException(ErrorCode.QUIZ_NOT_FOUND));
  }
}
