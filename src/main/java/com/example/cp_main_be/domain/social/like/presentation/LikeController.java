package com.example.cp_main_be.domain.social.like.presentation;

import com.example.cp_main_be.domain.social.like.dto.response.LikeCountResponse;
import com.example.cp_main_be.domain.social.like.service.LikeService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class LikeController {

  private final LikeService likeService;
  private final UserService userService;

  @PostMapping("/diaries/{diaryId}/likes")
  public ResponseEntity<ApiResponse<LikeCountResponse>> likeDiary(@PathVariable Long diaryId) {
    User user = userService.getCurrentUser();
    int likeCount = likeService.addLike(user.getId(), diaryId, "DIARY");
    return ResponseEntity.ok(ApiResponse.success(new LikeCountResponse(likeCount)));
  }

  @DeleteMapping("/diaries/{diaryId}/likes")
  public ResponseEntity<ApiResponse<LikeCountResponse>> unlikeDiary(@PathVariable Long diaryId) {
    User user = userService.getCurrentUser();
    int likeCount = likeService.removeLike(user.getId(), diaryId, "DIARY");
    return ResponseEntity.ok(ApiResponse.success(new LikeCountResponse(likeCount)));
  }

  @PostMapping("/avatar-posts/{postId}/likes")
  public ResponseEntity<ApiResponse<LikeCountResponse>> likeAvatarPost(@PathVariable Long postId) {
    User user = userService.getCurrentUser();
    int likeCount = likeService.addLike(user.getId(), postId, "AVATAR_POST");
    return ResponseEntity.ok(ApiResponse.success(new LikeCountResponse(likeCount)));
  }

  @DeleteMapping("/avatar-posts/{postId}/likes")
  public ResponseEntity<ApiResponse<LikeCountResponse>> unlikeAvatarPost(@PathVariable Long postId) {
    User user = userService.getCurrentUser();
    int likeCount = likeService.removeLike(user.getId(), postId, "AVATAR_POST");
    return ResponseEntity.ok(ApiResponse.success(new LikeCountResponse(likeCount)));
  }
}
