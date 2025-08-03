package com.example.cp_main_be.domain.social.like.presentation;

import com.example.cp_main_be.domain.social.like.service.LikeService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LikeController {

  private final LikeService likeService;
  private final UserService userService;

  @PostMapping("/diaries/{diaryId}/likes")
  public ResponseEntity<ApiResponse<Void>> likeDiary(@PathVariable Long diaryId) {
    String userUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = userService.findUserByUuid(UUID.fromString(userUuid));
    likeService.addLike(user.getId(), diaryId, "DIARY");
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @DeleteMapping("/diaries/{diaryId}/likes")
  public ResponseEntity<ApiResponse<Void>> unlikeDiary(@PathVariable Long diaryId) {
    String userUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = userService.findUserByUuid(UUID.fromString(userUuid));
    likeService.removeLike(user.getId(), diaryId, "DIARY");
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PostMapping("/avatar-posts/{postId}/likes")
  public ResponseEntity<ApiResponse<Void>> likeAvatarPost(@PathVariable Long postId) {
    String userUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = userService.findUserByUuid(UUID.fromString(userUuid));
    likeService.addLike(user.getId(), postId, "AVATAR_POST");
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @DeleteMapping("/avatar-posts/{postId}/likes")
  public ResponseEntity<ApiResponse<Void>> unlikeAvatarPost(@PathVariable Long postId) {
    String userUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = userService.findUserByUuid(UUID.fromString(userUuid));
    likeService.removeLike(user.getId(), postId, "AVATAR_POST");
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
