package com.example.cp_main_be.domain.social.comment.service;

import com.example.cp_main_be.domain.notification.domain.NotificationType;
import com.example.cp_main_be.domain.notification.service.NotificationService;
import com.example.cp_main_be.domain.social.comment.domain.Comment;
import com.example.cp_main_be.domain.social.comment.domain.repository.CommentRepository;
import com.example.cp_main_be.domain.social.comment.dto.request.CommentRequest;
import com.example.cp_main_be.domain.social.feed.domain.Feed;
import com.example.cp_main_be.domain.social.feed.domain.repository.FeedRepository;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final FeedRepository feedRepository;
  private final NotificationService notificationService;

  public Comment createComment(Long writerId, CommentRequest request) {
    User writer =
        userRepository
            .findById(writerId)
            .orElseThrow(() -> new UserNotFoundException("작성자 사용자를 찾을 수 없습니다."));

    Comment comment =
        Comment.builder()
            .writer(writer)
            .content(request.getContent())
            .targetId(request.getTargetId())
            .targetType(request.getTargetType())
            .build();
    commentRepository.save(comment);

    if ("feed".equalsIgnoreCase(request.getTargetType())) {
      Feed feed =
          feedRepository
              .findById(request.getTargetId())
              .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));
      User receiver = feed.getUser();

      // 자기 자신에게는 알림을 보내지 않음
      if (!receiver.getId().equals(writerId)) {
        notificationService.send(
            receiver, writer, NotificationType.FEED_COMMENT, "/feeds/" + request.getTargetId());
      }
    }
    return comment;
  }

  public Comment updateComment(Long commentId, Long writerId, CommentRequest request) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다.")); // TODO: Custom Exception

    if (!comment.getWriter().getId().equals(writerId)) {
      throw new RuntimeException("댓글 작성자만 수정할 수 있습니다."); // TODO: Custom Exception
    }

    comment.setContent(request.getContent());
    return commentRepository.save(comment);
  }

  public void deleteComment(Long commentId, Long writerId) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다.")); // TODO: Custom Exception

    if (!comment.getWriter().getId().equals(writerId)) {
      throw new RuntimeException("댓글 작성자만 삭제할 수 있습니다."); // TODO: Custom Exception
    }
    commentRepository.delete(comment);
  }
}
