package com.example.cp_main_be.domain.social.comment.presentation;

import com.example.cp_main_be.domain.social.comment.dto.request.CommentRequest;
import com.example.cp_main_be.domain.social.comment.service.CommentService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
  public ResponseEntity<ApiResponse<Void>> createComment(
      @RequestBody @Valid CommentRequest request) {
    String writerUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User writer = userService.findUserByUuid(UUID.fromString(writerUuid));
    commentService.createComment(writer.getId(), request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "댓글 수정", description = "댓글을 수정합니다")
  @PutMapping("/{commentId}")
  public ResponseEntity<ApiResponse<Void>> updateComment(
      @PathVariable Long commentId, @RequestBody @Valid CommentRequest request) {
    String writerUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User writer = userService.findUserByUuid(UUID.fromString(writerUuid));
    commentService.updateComment(commentId, writer.getId(), request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다")
  @DeleteMapping("/{commentId}")
  public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
    String writerUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User writer = userService.findUserByUuid(UUID.fromString(writerUuid));
    commentService.deleteComment(commentId, writer.getId());
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
