package com.example.cp_main_be.domain.reports.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.reports.domain.ReportReason;
import com.example.cp_main_be.domain.reports.domain.Reports;
import com.example.cp_main_be.domain.reports.domain.repository.ReportReasonRepository;
import com.example.cp_main_be.domain.reports.domain.repository.ReportRepository;
import com.example.cp_main_be.domain.reports.dto.ReportRequestDto;
import com.example.cp_main_be.domain.reports.enums.ReportStatus;
import com.example.cp_main_be.domain.social.comment.domain.Comment;
import com.example.cp_main_be.domain.social.comment.domain.repository.CommentRepository;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.repository.DiaryRepository;
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
      case USER:
        return reportRequestDto.getTargetId(); // When reporting a user, targetId is the userId
      default:
        throw new IllegalArgumentException("Invalid report target type");
    }
  }
}
