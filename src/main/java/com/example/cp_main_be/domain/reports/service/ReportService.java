package com.example.cp_main_be.domain.reports.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.userblock.BlockService;
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
  private final BlockService blockService;
  private final com.example.cp_main_be.domain.member.notification.service.NotificationService
      notificationService; // [추가]

  public void createReport(Long reporterId, ReportRequestDto reportRequestDto) {
    User reporter =
        userRepository
            .findById(reporterId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

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
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

    // [추가] 신고한 사용자가 신고 대상자를 즉시 차단 (노출 차단 목적)
    // 차단 시 상호 팔로우 해제까지 처리
    if (!blockService.isBlocked(reporter, reportedUser)) {
      blockService.blockUser(reporter, reportedUserId);
    }

    notificationService.send(
        reportedUser,
        null, // sender는 null (익명/시스템)
        com.example.cp_main_be.domain.member.notification.domain.NotificationType.REPORT_RECEIVED,
        "/reports/" + report.getId(), // 알림 클릭 시 이동할 URL
        null // 썸네일 없음
        );

    // 신고자에게 접수 완료 알림 발송
    notificationService.send(
        reporter,
        null, // sender는 null (익명/시스템)
        com.example.cp_main_be.domain.member.notification.domain.NotificationType.REPORT_SUBMITTED,
        "/reports/" + report.getId(),
        null);

    // 신고 직후 차단 처리로 신고 대상자 콘텐츠가 신고자에게 숨겨짐
  }

  private Long findReportedUserId(ReportRequestDto reportRequestDto) {
    switch (reportRequestDto.getTargetType()) {
      case DIARY:
        Diary diary =
            diaryRepository
                .findById(reportRequestDto.getTargetId())
                .orElseThrow(() -> new CustomApiException(ErrorCode.DIARY_NOT_FOUND));
        return diary.getUser().getId();
      case COMMENT:
        Comment comment =
            commentRepository
                .findById(reportRequestDto.getTargetId())
                .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));
        return comment.getWriter().getId();
      case AVATAR_POST:
        AvatarPost avatarPost =
            avatarPostRepository
                .findById(reportRequestDto.getTargetId())
                .orElseThrow(() -> new CustomApiException(ErrorCode.POST_NOT_FOUND));
        return avatarPost.getUser().getId();
      case USER:
        return reportRequestDto.getTargetId(); // When reporting a user, targetId is the userId
      default:
        throw new CustomApiException(ErrorCode.INVALID_REQUEST, "지원하지 않는 신고 대상입니다.");
    }
  }
}
