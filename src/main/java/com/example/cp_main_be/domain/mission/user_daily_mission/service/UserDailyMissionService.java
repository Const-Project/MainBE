package com.example.cp_main_be.domain.mission.user_daily_mission.service;

import com.example.cp_main_be.domain.avatar.image.DailyMissionImage;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.domain.mission.daily_mission_master.MissionType;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import com.example.cp_main_be.domain.mission.daily_mission_master.dto.response.DailyMissionResponseDTO;
import com.example.cp_main_be.domain.mission.quiz.domain.Quiz;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizOptionsRepository;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizRepository;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserDailyMission;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserImageMission;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.repository.UserDailyMissionRepository;
import com.example.cp_main_be.domain.mission.user_daily_mission.dto.MissionPanelResponse;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.infra.S3Uploader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
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
  private final UserRepository userRepository;

  public DailyMissionResponseDTO getDailyMissions(Long userId) {
    List<UserDailyMission> dailyMissions = userDailyMissionRepository.findAllByUser_Id(userId);

    List<DailyMissionMaster> dailyMissionMasters =
        dailyMissions.stream().map(UserDailyMission::getDailyMissionMaster).toList();

    return DailyMissionResponseDTO.from(dailyMissionMasters);
  }

  public String uploadPictureForDailyMission(Long userDailyMissionId, MultipartFile file) {
    UserImageMission userDailyMission =
        (UserImageMission)
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
      userService.addExperience(userService.getCurrentUser().getId(), PHOTO_MISSION_COMPLETE_POINT);
    else if (type.equals(MissionType.QUIZ))
      userService.addExperience(userService.getCurrentUser().getId(), QUIZ_MISSION_COMPLETE_POINT);
    else
      userService.addExperience(userService.getCurrentUser().getId(), DIARY_MISSION_COMPLETE_POINT);
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

  // [새로 추가될 서비스 메서드]
  public MissionPanelResponse getMissionPanelData(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));

    // 1. 오늘의 미션 목록 조회 (N+1 방지)
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
    List<UserDailyMission> todayMissions =
        userDailyMissionRepository.findTodayMissionsWithMasterByUser(user, startOfDay, endOfDay);

    List<MissionPanelResponse.DailyMissionStatusDto> missionDtos =
        todayMissions.stream()
            .map(
                mission ->
                    MissionPanelResponse.DailyMissionStatusDto.builder()
                        .userDailyMissionId(mission.getId())
                        .missionType(mission.getDailyMissionMaster().getMissionType().toString())
                        .title(mission.getDailyMissionMaster().getTitle())
                        .isCompleted(mission.isCompleted())
                        .build())
            .collect(Collectors.toList());
    Integer count = 0;
    for (MissionPanelResponse.DailyMissionStatusDto mission : missionDtos) {
      if (mission.isCompleted()) count++;
    }

    // 2. 소망 나무 정보 조회
    // TODO: WishTree 엔티티 및 Repository 구현 후 실제 데이터 조회 로직 필요
    MissionPanelResponse.WishTreeDto wishTreeDto =
        MissionPanelResponse.WishTreeDto.builder()
            .currentStage("꽃") // 예시 데이터
            .currentPoints(1200) // 예시 데이터
            .requiredPointsForNextStage(1300) // 예시 데이터
            .build();

    // 3. 최종 응답 조립
    return MissionPanelResponse.builder()
        .todayMissionCount(count)
        .dailyMissions(missionDtos)
        .wishTree(wishTreeDto)
        .build();
  }
}
