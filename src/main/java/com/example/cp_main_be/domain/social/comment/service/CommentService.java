package com.example.cp_main_be.domain.social.comment.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.comment.domain.Comment;
import com.example.cp_main_be.domain.social.comment.domain.repository.CommentRepository;
import com.example.cp_main_be.domain.social.comment.dto.request.CommentRequest;
import com.example.cp_main_be.domain.social.comment.dto.request.UpdateCommentRequest;
import com.example.cp_main_be.domain.social.comment.dto.response.CommentResponse;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.repository.DiaryRepository;
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
              .orElseThrow(
                  () -> new IllegalArgumentException("ID에 해당하는 일기를 찾을 수 없습니다: " + targetId));
      commentBuilder.diary(diary);
    } else if ("AVATAR_POST".equalsIgnoreCase(targetType)) {
      AvatarPost avatarPost =
          avatarPostRepository
              .findById(targetId)
              .orElseThrow(
                  () -> new IllegalArgumentException("ID에 해당하는 아바타 포스트를 찾을 수 없습니다: " + targetId));
      commentBuilder.avatarPost(avatarPost);
    } else {
      throw new IllegalArgumentException("지원하지 않는 대상 타입입니다: " + targetType);
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
            .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다.")); // TODO: Custom Exception

    if (!comment.getWriter().getId().equals(writerId)) {
      throw new RuntimeException("댓글 작성자만 수정할 수 있습니다."); // TODO: Custom Exception
    }

    comment.setContent(request.getContent());
    commentRepository.save(comment);

    return getResponse(comment);
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
