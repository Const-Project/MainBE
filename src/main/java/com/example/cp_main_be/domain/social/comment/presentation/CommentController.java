package com.example.cp_main_be.domain.social.comment.presentation;

import com.example.cp_main_be.domain.social.comment.dto.request.CommentRequest;
import com.example.cp_main_be.domain.social.comment.service.CommentService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentController {

  private final CommentService commentService;
  private final UserService userService;

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createComment(
      @RequestBody @Valid CommentRequest request) {
    String writerUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User writer = userService.findUserByUuid(UUID.fromString(writerUuid));
    commentService.createComment(writer.getId(), request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PutMapping("/{commentId}")
  public ResponseEntity<ApiResponse<Void>> updateComment(
      @PathVariable Long commentId, @RequestBody @Valid CommentRequest request) {
    String writerUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User writer = userService.findUserByUuid(UUID.fromString(writerUuid));
    commentService.updateComment(commentId, writer.getId(), request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
    String writerUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User writer = userService.findUserByUuid(UUID.fromString(writerUuid));
    commentService.deleteComment(commentId, writer.getId());
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
