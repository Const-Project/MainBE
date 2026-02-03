package com.example.cp_main_be.domain.social.comment.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.userblock.UserBlockRepository;
import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import com.example.cp_main_be.domain.mission.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.comment.domain.Comment;
import com.example.cp_main_be.domain.social.comment.domain.repository.CommentRepository;
import com.example.cp_main_be.domain.social.comment.dto.request.CommentRequest;
import com.example.cp_main_be.domain.social.comment.dto.request.UpdateCommentRequest;
import com.example.cp_main_be.domain.social.comment.dto.response.CommentResponse;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.event.CommentCreatedEvent;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final DiaryRepository diaryRepository; // Diary Repository 주입
  private final AvatarPostRepository avatarPostRepository; // AvatarPost Repository 주입
  private final UserBlockRepository userBlockRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public CommentResponse createComment(Long writerId, CommentRequest request) {
    // 1. 댓글 작성자 조회
    User writer =
        userRepository
            .findById(writerId)
            .orElseThrow(() -> new UserNotFoundException("작성자 사용자를 찾을 수 없습니다."));

    // 2. Comment 빌더 준비
    Comment.CommentBuilder commentBuilder =
        Comment.builder().writer(writer).content(request.getContent());

    // 3. targetType에 따라 분기하여 부모 엔티티를 찾고 연관관계를 설정
    String targetType = request.getTargetType();
    Long targetId = request.getTargetId();

    if ("DIARY".equalsIgnoreCase(targetType)) {
      Diary diary =
          diaryRepository
              .findById(targetId)
              .orElseThrow(() -> new CustomApiException(ErrorCode.DIARY_NOT_FOUND));
      if (userBlockRepository.existsByBlockerUserAndBlockedUser(diary.getUser(), writer)) {
        throw new CustomApiException(ErrorCode.ACCESS_DENIED, "차단한 사용자에게 댓글을 달 수 없습니다.");
      }
      commentBuilder.diary(diary);
    } else if ("AVATAR_POST".equalsIgnoreCase(targetType)) {
      AvatarPost avatarPost =
          avatarPostRepository
              .findById(targetId)
              .orElseThrow(() -> new CustomApiException(ErrorCode.POST_NOT_FOUND));
      if (userBlockRepository.existsByBlockerUserAndBlockedUser(avatarPost.getUser(), writer)) {
        throw new CustomApiException(ErrorCode.ACCESS_DENIED, "차단한 사용자에게 댓글을 달 수 없습니다.");
      }
      commentBuilder.avatarPost(avatarPost);
    } else {
      throw new CustomApiException(ErrorCode.INVALID_REQUEST, "지원하지 않는 대상 타입입니다.");
    }

    // 4. 최종적으로 Comment 객체를 빌드하고 저장
    Comment comment = commentBuilder.build();
    commentRepository.save(comment);

    // 5. 이벤트 발행
    eventPublisher.publishEvent(new CommentCreatedEvent(comment));

    return getResponse(comment);
  }

  public CommentResponse updateComment(
      Long commentId, Long writerId, UpdateCommentRequest request) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));

    if (!comment.getWriter().getId().equals(writerId)) {
      throw new CustomApiException(ErrorCode.ACCESS_DENIED, "댓글 작성자만 수정할 수 있습니다.");
    }

    comment.setContent(request.getContent());
    commentRepository.save(comment);

    return getResponse(comment);
  }

  public void deleteComment(Long commentId, Long writerId) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));

    if (!comment.getWriter().getId().equals(writerId)) {
      throw new CustomApiException(ErrorCode.ACCESS_DENIED, "댓글 작성자만 삭제할 수 있습니다.");
    }
    commentRepository.delete(comment);
  }

  private CommentResponse getResponse(Comment comment) {
    Long targetId;
    String targetType;

    if (comment.getDiary() != null) {
      targetId = comment.getDiary().getId();
      targetType = "DIARY";
    } else if (comment.getAvatarPost() != null) {
      targetId = comment.getAvatarPost().getId();
      targetType = "AVATAR_POST";
    } else {
      throw new IllegalStateException("Comment must be linked to either Diary or AvatarPost");
    }

    return CommentResponse.from(comment, targetId, targetType);
  }
}
