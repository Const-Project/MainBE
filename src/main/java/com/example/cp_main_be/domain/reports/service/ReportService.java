package com.example.cp_main_be.domain.reports.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.userblock.UserBlock;
import com.example.cp_main_be.domain.member.userblock.UserBlockRepository;
import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import com.example.cp_main_be.domain.mission.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.reports.domain.ReportReason;
import com.example.cp_main_be.domain.reports.domain.Reports;
import com.example.cp_main_be.domain.reports.domain.repository.ReportReasonRepository;
import com.example.cp_main_be.domain.reports.domain.repository.ReportRepository;
import com.example.cp_main_be.domain.reports.dto.ReportRequestDto;
import com.example.cp_main_be.domain.reports.enums.ReportStatus;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.comment.domain.Comment;
import com.example.cp_main_be.domain.social.comment.domain.repository.CommentRepository;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

  private final ReportRepository reportRepository;
  private final UserRepository userRepository;
  private final ReportReasonRepository reportReasonRepository;
  private final DiaryRepository diaryRepository; // Assuming Diary can be reported
  private final CommentRepository commentRepository; // Assuming Comment can be reported
  private final AvatarPostRepository avatarPostRepository;
  private final UserBlockRepository userBlockRepository;
  private final com.example.cp_main_be.domain.member.notification.service.NotificationService
      notificationService; // [추가]

  public void createReport(Long reporterId, ReportRequestDto reportRequestDto) {
    User reporter =
        userRepository
            .findById(reporterId)
            .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

    ReportReason reason =
        reportReasonRepository
            .findByReasonText(reportRequestDto.getReason())
            .orElseGet(
                () ->
                    reportReasonRepository.save(
                        ReportReason.builder().reasonText(reportRequestDto.getReason()).build()));

    Long reportedUserId = findReportedUserId(reportRequestDto);

    if (reporterId.equals(reportedUserId)) {
      throw new CustomApiException(ErrorCode.INVALID_REQUEST, "자기 자신을 신고할 수 없습니다.");
    }

    if (reportRepository.existsByUserAndTargetTypeAndTargetIdAndStatus(
        reporter,
        reportRequestDto.getTargetType(),
        reportRequestDto.getTargetId(),
        ReportStatus.PENDING)) {
      throw new CustomApiException(ErrorCode.INVALID_REQUEST, "이미 신고한 대상입니다.");
    }

    Reports report =
        Reports.builder()
            .user(reporter)
            .targetType(reportRequestDto.getTargetType())
            .targetId(reportRequestDto.getTargetId())
            .reportedUserId(reportedUserId)
            .reason(reason)
            .additionalComment(reportRequestDto.getAdditionalComment())
            .status(ReportStatus.PENDING)
            .build();

    reportRepository.save(report);

    // [추가] 신고 대상자에게 익명 알림 발송
    // reportedUser 엔티티를 찾아야 함
    User reportedUser =
        userRepository
            .findById(reportedUserId)
            .orElseThrow(() -> new UserNotFoundException("신고 대상자를 찾을 수 없습니다."));

    // [추가] 신고한 사용자가 신고 대상자를 즉시 차단 (노출 차단 목적)
    if (!userBlockRepository.existsByBlockerUserAndBlockedUser(reporter, reportedUser)) {
      UserBlock userBlock =
          UserBlock.builder().blockerUser(reporter).blockedUser(reportedUser).build();
      userBlockRepository.save(userBlock);
    }

    notificationService.send(
        reportedUser,
        null, // sender는 null (익명/시스템)
        com.example.cp_main_be.domain.member.notification.domain.NotificationType.REPORT_RECEIVED,
        "/report/received/" + report.getId(), // 알림 클릭 시 이동할 URL (예시)
        null // 썸네일 없음
        );

    // TODO: Implement logic to hide content from the reporter
    // TODO: Implement logic to block the user if a user is reported
  }

  private Long findReportedUserId(ReportRequestDto reportRequestDto) {
    switch (reportRequestDto.getTargetType()) {
      case DIARY:
        Diary diary =
            diaryRepository
                .findById(reportRequestDto.getTargetId())
                .orElseThrow(() -> new IllegalArgumentException("Diary not found"));
        return diary.getUser().getId();
      case COMMENT:
        Comment comment =
            commentRepository
                .findById(reportRequestDto.getTargetId())
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        return comment.getWriter().getId();
      case AVATAR_POST:
        AvatarPost avatarPost =
            avatarPostRepository
                .findById(reportRequestDto.getTargetId())
                .orElseThrow(() -> new IllegalArgumentException("Avatar post not found"));
        return avatarPost.getUser().getId();
      case USER:
        return reportRequestDto.getTargetId(); // When reporting a user, targetId is the userId
      default:
        throw new IllegalArgumentException("Invalid report target type");
    }
  }
}
