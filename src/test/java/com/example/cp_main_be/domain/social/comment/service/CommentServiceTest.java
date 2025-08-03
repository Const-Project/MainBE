package com.example.cp_main_be.domain.social.comment.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.cp_main_be.domain.social.comment.domain.Comment;
import com.example.cp_main_be.domain.social.comment.domain.repository.CommentRepository;
import com.example.cp_main_be.domain.social.comment.dto.request.CommentRequest;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

  @Mock private CommentRepository commentRepository;
  @Mock private UserRepository userRepository;

  @InjectMocks private CommentService commentService;

  @DisplayName("댓글 생성 성공")
  @Test
  void createComment_success() {
    // given
    Long writerId = 1L;
    CommentRequest request = new CommentRequest();
    request.setContent("테스트 댓글");
    request.setTargetId(10L);
    request.setTargetType("DIARY");

    User writer = User.builder().id(writerId).username("writer").build();

    given(userRepository.findById(writerId)).willReturn(Optional.of(writer));
    given(commentRepository.save(any(Comment.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // when
    Comment createdComment = commentService.createComment(writerId, request);

    // then
    Assertions.assertNotNull(createdComment);
    Assertions.assertEquals(request.getContent(), createdComment.getContent());
    Assertions.assertEquals(writerId, createdComment.getWriter().getId());
    Assertions.assertEquals(request.getTargetId(), createdComment.getTargetId());
    Assertions.assertEquals(request.getTargetType(), createdComment.getTargetType());
    verify(commentRepository).save(any(Comment.class));
  }

  @DisplayName("댓글 생성 실패 - 작성자 없음")
  @Test
  void createComment_fail_writerNotFound() {
    // given
    Long writerId = 1L;
    CommentRequest request = new CommentRequest();
    request.setContent("테스트 댓글");
    request.setTargetId(10L);
    request.setTargetType("DIARY");

    given(userRepository.findById(writerId)).willReturn(Optional.empty());

    // when & then
    Assertions.assertThrows(
        UserNotFoundException.class, () -> commentService.createComment(writerId, request));
    verify(commentRepository, org.mockito.Mockito.never()).save(any(Comment.class));
  }

  @DisplayName("댓글 수정 성공")
  @Test
  void updateComment_success() {
    // given
    Long commentId = 1L;
    Long writerId = 1L;
    String oldContent = "이전 댓글";
    String newContent = "수정된 댓글";
    CommentRequest request = new CommentRequest();
    request.setContent(newContent);

    User writer = User.builder().id(writerId).username("writer").build();
    Comment comment = Comment.builder().id(commentId).writer(writer).content(oldContent).build();

    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
    given(commentRepository.save(any(Comment.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // when
    Comment updatedComment = commentService.updateComment(commentId, writerId, request);

    // then
    Assertions.assertNotNull(updatedComment);
    Assertions.assertEquals(newContent, updatedComment.getContent());
    verify(commentRepository).findById(commentId);
    verify(commentRepository).save(any(Comment.class));
  }

  @DisplayName("댓글 수정 실패 - 댓글을 찾을 수 없음")
  @Test
  void updateComment_fail_commentNotFound() {
    // given
    Long commentId = 1L;
    Long writerId = 1L;
    CommentRequest request = new CommentRequest();
    request.setContent("수정된 댓글");

    given(commentRepository.findById(commentId)).willReturn(Optional.empty());

    // when & then
    Assertions.assertThrows(
        RuntimeException.class, () -> commentService.updateComment(commentId, writerId, request));
    verify(commentRepository, org.mockito.Mockito.never()).save(any(Comment.class));
  }

  @DisplayName("댓글 수정 실패 - 작성자가 아님")
  @Test
  void updateComment_fail_notWriter() {
    // given
    Long commentId = 1L;
    Long writerId = 1L;
    Long otherUserId = 2L;
    CommentRequest request = new CommentRequest();
    request.setContent("수정된 댓글");

    User writer = User.builder().id(writerId).username("writer").build();
    Comment comment = Comment.builder().id(commentId).writer(writer).content("이전 댓글").build();

    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    // when & then
    Assertions.assertThrows(
        RuntimeException.class,
        () -> commentService.updateComment(commentId, otherUserId, request));
    verify(commentRepository, org.mockito.Mockito.never()).save(any(Comment.class));
  }

  @DisplayName("댓글 삭제 성공")
  @Test
  void deleteComment_success() {
    // given
    Long commentId = 1L;
    Long writerId = 1L;

    User writer = User.builder().id(writerId).username("writer").build();
    Comment comment = Comment.builder().id(commentId).writer(writer).content("댓글 내용").build();

    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    // when
    commentService.deleteComment(commentId, writerId);

    // then
    verify(commentRepository).findById(commentId);
    verify(commentRepository).delete(comment);
  }

  @DisplayName("댓글 삭제 실패 - 댓글을 찾을 수 없음")
  @Test
  void deleteComment_fail_commentNotFound() {
    // given
    Long commentId = 1L;
    Long writerId = 1L;

    given(commentRepository.findById(commentId)).willReturn(Optional.empty());

    // when & then
    Assertions.assertThrows(
        RuntimeException.class, () -> commentService.deleteComment(commentId, writerId));
    verify(commentRepository).findById(commentId);
    verify(commentRepository, org.mockito.Mockito.never()).delete(any(Comment.class));
  }

  @DisplayName("댓글 삭제 실패 - 작성자가 아님")
  @Test
  void deleteComment_fail_notWriter() {
    // given
    Long commentId = 1L;
    Long writerId = 1L;
    Long otherUserId = 2L;

    User writer = User.builder().id(writerId).username("writer").build();
    Comment comment = Comment.builder().id(commentId).writer(writer).content("댓글 내용").build();

    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    // when & then
    Assertions.assertThrows(
        RuntimeException.class, () -> commentService.deleteComment(commentId, otherUserId));
    verify(commentRepository).findById(commentId);
    verify(commentRepository, org.mockito.Mockito.never()).delete(any(Comment.class));
  }
}
