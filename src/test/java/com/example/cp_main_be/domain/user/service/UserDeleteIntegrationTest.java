package com.example.cp_main_be.domain.user.service;

import static org.assertj.core.api.Assertions.assertThatNoException;

import com.example.cp_main_be.domain.delivery.domain.Delivery;
import com.example.cp_main_be.domain.delivery.domain.DeliveryPlant;
import com.example.cp_main_be.domain.delivery.domain.repository.DeliveryPlantRepository;
import com.example.cp_main_be.domain.delivery.domain.repository.DeliveryRepository;
import com.example.cp_main_be.domain.member.daily_question.domain.AnswerType;
import com.example.cp_main_be.domain.member.daily_question.domain.DailyQuestionAnswer;
import com.example.cp_main_be.domain.member.daily_question.domain.repository.DailyQuestionAnswerRepository;
import com.example.cp_main_be.domain.member.log.domain.UserDailyActivityLog;
import com.example.cp_main_be.domain.member.log.domain.repository.UserDailyActivityLogRepository;
import com.example.cp_main_be.domain.member.notification.domain.repository.EmitterRepository;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.domain.member.userblock.UserBlock;
import com.example.cp_main_be.domain.member.userblock.UserBlockRepository;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.repository.DailyMissionMasterRepository;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.UserQuizMission;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.repository.UserDailyMissionRepository;
import com.example.cp_main_be.domain.mission.wishTree.WishTreeService;
import com.example.cp_main_be.domain.realquiz.RealQuiz;
import com.example.cp_main_be.domain.realquiz.UserQuiz;
import com.example.cp_main_be.domain.realquiz.repository.RealQuizRepostitory;
import com.example.cp_main_be.domain.realquiz.repository.UserQuizRepository;
import com.example.cp_main_be.domain.reports.domain.Reports;
import com.example.cp_main_be.domain.reports.domain.repository.ReportRepository;
import com.example.cp_main_be.domain.social.comment.domain.Comment;
import com.example.cp_main_be.domain.social.comment.domain.repository.CommentRepository;
import com.example.cp_main_be.domain.tracking.domain.TrackingReportView;
import com.example.cp_main_be.domain.tracking.domain.repository.TrackingReportViewRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@Import(UserService.class)
class UserDeleteIntegrationTest {

  @MockitoBean private WishTreeService wishTreeService;
  @MockitoBean private EmitterRepository emitterRepository;

  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private UserDailyActivityLogRepository userDailyActivityLogRepository;
  @Autowired private DeliveryRepository deliveryRepository;
  @Autowired private UserQuizRepository userQuizRepository;
  @Autowired private TrackingReportViewRepository trackingReportViewRepository;
  @Autowired private DailyQuestionAnswerRepository dailyQuestionAnswerRepository;
  @Autowired private UserDailyMissionRepository userDailyMissionRepository;
  @Autowired private ReportRepository reportRepository;
  @Autowired private UserBlockRepository userBlockRepository;
  @Autowired private CommentRepository commentRepository;
  @Autowired private DeliveryPlantRepository deliveryPlantRepository;
  @Autowired private DailyMissionMasterRepository dailyMissionMasterRepository;
  @Autowired private RealQuizRepostitory realQuizRepository;

  @DisplayName("deleteUser: 연관 FK 데이터가 있는 사용자 탈퇴 시 FK 에러 없이 완료")
  @Test
  void deleteUser_연관FK데이터_있어도_에러없이_완료() {
    User user =
        userRepository.save(User.builder().nickname("탈퇴유저").uuid(UUID.randomUUID()).build());
    User other =
        userRepository.save(User.builder().nickname("다른유저").uuid(UUID.randomUUID()).build());

    userDailyActivityLogRepository.save(
        UserDailyActivityLog.builder()
            .user(user)
            .date(LocalDate.now())
            .hasWatered(false)
            .hasSunlight(false)
            .build());

    dailyQuestionAnswerRepository.save(
        DailyQuestionAnswer.builder()
            .user(user)
            .question("테스트 질문?")
            .answer(AnswerType.YES)
            .answeredDate(LocalDate.now())
            .build());

    trackingReportViewRepository.save(
        TrackingReportView.builder()
            .user(user)
            .cycleKey("2024-W01")
            .viewedAt(LocalDateTime.now())
            .build());

    reportRepository.save(Reports.builder().user(user).build());

    userBlockRepository.save(UserBlock.builder().blockerUser(user).blockedUser(other).build());
    userBlockRepository.save(UserBlock.builder().blockerUser(other).blockedUser(user).build());

    DeliveryPlant plant =
        deliveryPlantRepository.save(DeliveryPlant.builder().name("테스트 식물").build());
    deliveryRepository.save(
        Delivery.builder()
            .user(user)
            .deliveryPlant(plant)
            .recipientName("홍길동")
            .recipientPhone("010-0000-0000")
            .postalCode("12345")
            .address("서울시 테스트구 테스트동")
            .build());

    RealQuiz quiz = realQuizRepository.save(RealQuiz.builder().build());
    userQuizRepository.save(UserQuiz.builder().user(user).realQuiz(quiz).build());

    DailyMissionMaster master =
        dailyMissionMasterRepository.save(DailyMissionMaster.builder().build());
    userDailyMissionRepository.save(
        UserQuizMission.builder().user(user).dailyMissionMaster(master).build());

    commentRepository.save(Comment.builder().writer(user).content("테스트 댓글").build());

    assertThatNoException().isThrownBy(() -> userService.deleteUser(user.getId()));
  }
}
