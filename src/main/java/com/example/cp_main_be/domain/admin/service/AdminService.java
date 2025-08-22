package com.example.cp_main_be.domain.admin.service;

import com.example.cp_main_be.domain.admin.dto.AdminRequestDTO;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.domain.mission.daily_keywords.domain.DailyKeywords;
import com.example.cp_main_be.domain.mission.daily_keywords.domain.repository.DailyKeywordsRepository;
import com.example.cp_main_be.domain.mission.daily_mission_master.MissionType;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.repository.DailyMissionMastersRepository;
import com.example.cp_main_be.domain.mission.quiz.domain.QuizOptions;
import com.example.cp_main_be.domain.reports.domain.Reports;
import com.example.cp_main_be.domain.reports.domain.repository.ReportRepository;
import com.example.cp_main_be.domain.reports.enums.ReportStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

  private final DailyMissionMastersRepository dailyMissionMastersRepository;
  private final DailyKeywordsRepository dailyKeywordsRepository;

  private final UserService userService;
  private final ReportRepository reportRepository;

  public DailyMissionMaster createDailyMissionMasters(
      AdminRequestDTO.CreateMissionRequestDTO requestDTO) {
    DailyMissionMaster dailyMissionMaster =
        DailyMissionMaster.builder()
            .title(requestDTO.getTitle())
            .description(requestDTO.getDescription())
            .content(requestDTO.getContent())
            .missionType(requestDTO.getMissionType())
            .rewardPoints(requestDTO.getRewardPoints())
            .build();

    return dailyMissionMastersRepository.save(dailyMissionMaster);
  }

  public DailyMissionMaster updateDailyMissionMasters(
      AdminRequestDTO.UpdateMissionRequestDTO requestDTO, Long id) {

    DailyMissionMaster dailyMissionMaster =
        dailyMissionMastersRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 ID의 미션을 찾을 수 없습니다."));

    // null 인 컬럼들은 수정 안한다.
    dailyMissionMaster.update(requestDTO);
    return dailyMissionMaster;
  }

  public DailyKeywords createDailyKeywords(AdminRequestDTO.CreateKeywordRequestDTO requestDTO) {

    DailyKeywords dailyKeyword = new DailyKeywords();
    dailyKeyword.from(requestDTO);
    return dailyKeywordsRepository.save(dailyKeyword);
  }

  public List<User> getUsers() {
    return userService.findAllUsers();
  }

  public User changeUserStatus(Long userId, AdminRequestDTO.ChangeUserStatusRequestDTO requestDTO) {
    User user = userService.findUserById(userId);
    user.setStatus(requestDTO.getUserStatus());
    return user;
  }

  public QuizOptions createQuizOption(AdminRequestDTO.CreateQuizRequestDTO requestDTO) {
    DailyMissionMaster dailyMissionMaster =
        dailyMissionMastersRepository
            .findById(requestDTO.getMissionMasterId())
            .orElseThrow(() -> new IllegalStateException("해당 ID를 가진 미션이 존재하지 않습니다."));

    if (dailyMissionMaster.getMissionType() != MissionType.QUIZ) {
      throw new IllegalArgumentException("퀴즈 타입의 미션에만 선지를 추가할 수 있습니다.");
    }

    return QuizOptions.builder() // QuizOptions 퀴즈의 선지
        .optionText(requestDTO.getOptionText())
        .optionOrder(requestDTO.getOptionOrder())
        .isCorrect(requestDTO.isCorrect())
        .build();
  }

  public List<Reports> getAllReports() {
    List<Reports> reports = reportRepository.findAll();
    return reports;
  }

  public Reports updateReportStatus(Long reportId, ReportStatus reportStatus) {
    Reports report =
        reportRepository
            .findById(reportId)
            .orElseThrow(() -> new RuntimeException("신고를 찾을 수 없습니다."));
    report.setStatus(reportStatus);
    return report;
  }
}
