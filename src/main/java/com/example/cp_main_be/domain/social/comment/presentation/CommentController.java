package com.example.cp_main_be.domain.social.comment.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.domain.social.comment.dto.request.CommentRequest;
import com.example.cp_main_be.domain.social.comment.dto.request.UpdateCommentRequest;
import com.example.cp_main_be.domain.social.comment.dto.response.CommentResponse;
import com.example.cp_main_be.domain.social.comment.service.CommentService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
@Tag(name = "댓글 API", description = "댓글 관련 기능을 제공합니다.")
public class CommentController {

  private final CommentService commentService;
  private final UserService userService;

  @Operation(summary = "댓글 달기", description = "targetId에 해당하는 Id를 가진 객체에 댓글을 답니다")
  @PostMapping
  public ResponseEntity<ApiResponse<CommentResponse>> createComment(
      @AuthenticationPrincipal User writer, @RequestBody @Valid CommentRequest request) {
    CommentResponse response = commentService.createComment(writer.getId(), request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "댓글 수정", description = "댓글을 수정합니다")
  @PutMapping("/{commentId}")
  public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
      @AuthenticationPrincipal User writer,
      @PathVariable Long commentId,
      @RequestBody @Valid UpdateCommentRequest request) {
    commentService.updateComment(commentId, writer.getId(), request);
    return ResponseEntity.ok(
        ApiResponse.success(commentService.updateComment(commentId, writer.getId(), request)));
  }

  @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다")
  @DeleteMapping("/{commentId}")
  public ResponseEntity<ApiResponse<Void>> deleteComment(
      @AuthenticationPrincipal User writer, @PathVariable Long commentId) {
    commentService.deleteComment(commentId, writer.getId());
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
