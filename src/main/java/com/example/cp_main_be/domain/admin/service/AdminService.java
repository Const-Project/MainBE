package com.example.cp_main_be.domain.admin.service;

import com.example.cp_main_be.domain.admin.dto.AdminRequestDTO;
import com.example.cp_main_be.domain.admin.dto.AdminResponseDTO;
import com.example.cp_main_be.domain.delivery.domain.DeliveryPlant;
import com.example.cp_main_be.domain.delivery.dto.request.DeliveryPlantRequest;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.domain.mission.daily_keywords.domain.DailyKeywords;
import com.example.cp_main_be.domain.mission.daily_keywords.domain.repository.DailyKeywordsRepository;
import com.example.cp_main_be.domain.mission.daily_mission_master.MissionType;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.repository.DailyMissionMastersRepository;
import com.example.cp_main_be.domain.mission.quiz.domain.Quiz;
import com.example.cp_main_be.domain.mission.quiz.domain.QuizOptions;
import com.example.cp_main_be.domain.mission.quiz.domain.repository.QuizRepository;
import com.example.cp_main_be.domain.reports.domain.Reports;
import com.example.cp_main_be.domain.reports.domain.repository.ReportRepository;
import com.example.cp_main_be.domain.reports.enums.ReportStatus;
import com.example.cp_main_be.global.exception.QuizNotFoundException;
import com.example.cp_main_be.global.infra.S3Uploader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

  private final DailyMissionMastersRepository dailyMissionMastersRepository;
  private final DailyKeywordsRepository dailyKeywordsRepository;

  private final UserService userService;
  private final ReportRepository reportRepository;
  private final S3Uploader s3Uploader;
  private final QuizRepository quizRepository;

  @Transactional
  public AdminResponseDTO.CreateQuizResponseDTO createQuiz(
      AdminRequestDTO.CreateQuizRequestDTO createQuizRequestDTO) {
    // 1. DailyMissionMaster 엔티티 생성
    DailyMissionMaster dailyMissionMaster =
        createDailyMissionMasters(createQuizRequestDTO.getForCreateMission());
    // 2. Quiz 엔티티 생성
    Quiz quiz =
        Quiz.builder()
            .answerNumber(createQuizRequestDTO.getAnswerNumber())
            .quizType(createQuizRequestDTO.getQuizType())
            .quizQuestion(createQuizRequestDTO.getQuizQuestion())
            .dailyMissionMaster(dailyMissionMaster)
            .build();
    quizRepository.save(quiz);
    // 3. QuizOption 엔티티 생성
    List<QuizOptions> quizOptionsList =
        createQuizRequestDTO.getQuizOptions().stream()
            .map(
                quizOption ->
                    QuizOptions.builder()
                        .quiz(quiz)
                        .optionText(quizOption.getOptionText())
                        .optionOrder(quizOption.getOptionOrder())
                        .build())
            .toList();
    List<AdminResponseDTO.QuizOptionsResponseDTO> quizOptionsResponseDTOS =
        quizOptionsList.stream()
            .map(
                quizOptions ->
                    AdminResponseDTO.QuizOptionsResponseDTO.builder()
                        .optionId(quizOptions.getId())
                        .optionText(quizOptions.getOptionText())
                        .optionOrder(quizOptions.getOptionOrder())
                        .build())
            .toList();
    // 4. 응답 DTO 생성
    return AdminResponseDTO.CreateQuizResponseDTO.builder()
        .quizId(quiz.getId())
        .quizQuestion(quiz.getQuizQuestion())
        .answerNumber(quiz.getAnswerNumber())
        .dailyMissionMaster(
            AdminResponseDTO.DailyMissionMastersResDTO.builder()
                .createdAt(dailyMissionMaster.getCreatedAt())
                .title(dailyMissionMaster.getTitle())
                .rewardPoints(dailyMissionMaster.getRewardPoints())
                .content(dailyMissionMaster.getContent())
                .description(dailyMissionMaster.getDescription())
                .missionType(dailyMissionMaster.getMissionType())
                .build())
        .quizOptions(quizOptionsResponseDTOS)
        .build();
  }

  @Transactional
  public boolean deleteQuiz(Long quizId) {
    Quiz quiz =
        quizRepository
            .findById(quizId)
            .orElseThrow(() -> new QuizNotFoundException("퀴즈가 존재하지 않습니다."));
    quizRepository.delete(quiz);
    return true;
  }

  // DailyMissionMasters 생성
  @Transactional
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

  @Transactional
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

  @Transactional
  public DailyKeywords createDailyKeywords(AdminRequestDTO.CreateKeywordRequestDTO requestDTO) {

    DailyKeywords dailyKeyword = new DailyKeywords();
    dailyKeyword.from(requestDTO);
    return dailyKeywordsRepository.save(dailyKeyword);
  }

  public List<User> getUsers() {
    return userService.findAllUsers();
  }

  @Transactional
  public User changeUserStatus(Long userId, AdminRequestDTO.ChangeUserStatusRequestDTO requestDTO) {
    User user = userService.findUserById(userId);
    user.setStatus(requestDTO.getUserStatus());
    return user;
  }

  @Transactional
  public QuizOptions createQuizOption(
      AdminRequestDTO.CreateQuizOptionRequestDTO requestDTO, Long quizId) {
    Quiz quiz =
        quizRepository
            .findById(quizId)
            .orElseThrow(() -> new RuntimeException("해당 ID를 가진 퀴즈가 존재하지 않습니다."));

    if (quiz.getDailyMissionMaster().getMissionType() != MissionType.QUIZ) {
      throw new IllegalArgumentException("퀴즈 타입의 미션에만 선지를 추가할 수 있습니다.");
    }

    return QuizOptions.builder() // QuizOptions 퀴즈의 선지
        .optionText(requestDTO.getOptionText())
        .optionOrder(requestDTO.getOptionOrder())
        .quiz(quiz)
        .build();
  }

  public List<Reports> getAllReports() {
    List<Reports> reports = reportRepository.findAll();
    return reports;
  }

  @Transactional
  public Reports updateReportStatus(Long reportId, ReportStatus reportStatus) {
    Reports report =
        reportRepository
            .findById(reportId)
            .orElseThrow(() -> new RuntimeException("신고를 찾을 수 없습니다."));
    report.setStatus(reportStatus);
    return report;
  }

  @Transactional
  public DeliveryPlant addDeliveryPlant(MultipartFile file, DeliveryPlantRequest request) {
    String imageUrl = s3Uploader.upload(file, "/deliveryplant");
    return DeliveryPlant.builder().name(request.getName()).imageUrl(imageUrl).build();
  }
}
