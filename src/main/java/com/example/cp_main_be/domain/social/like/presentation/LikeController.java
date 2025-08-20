package com.example.cp_main_be.domain.social.like.presentation;

import com.example.cp_main_be.domain.social.like.dto.response.LikeCountResponse;
import com.example.cp_main_be.domain.social.like.service.LikeService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "좋아요 API", description = "좋아요 관련 기능을 제공합니다.")
public class LikeController {

  private final LikeService likeService;
  private final UserService userService;

  @Operation(summary = "일기 좋아요", description = "다른 유저의 일기에 좋아요를 누릅니다")
  @PostMapping("/diaries/{diaryId}/likes")
  public ResponseEntity<ApiResponse<LikeCountResponse>> likeDiary(@PathVariable Long diaryId) {
    User user = userService.getCurrentUser();
    int likeCount = likeService.addLike(user.getId(), diaryId, "DIARY");
    return ResponseEntity.ok(ApiResponse.success(new LikeCountResponse(likeCount)));
  }

  @Operation(summary = "일기 좋아요 취소", description = "다른 유저의 일기에 누른 좋아요를 취소합니다")
  @DeleteMapping("/diaries/{diaryId}/likes")
  public ResponseEntity<ApiResponse<LikeCountResponse>> unlikeDiary(@PathVariable Long diaryId) {
    User user = userService.getCurrentUser();
    int likeCount = likeService.removeLike(user.getId(), diaryId, "DIARY");
    return ResponseEntity.ok(ApiResponse.success(new LikeCountResponse(likeCount)));
  }

  @Operation(summary = "아바타 포스트 좋아요", description = "다른 유저의 아바타 포스트에 좋아요를 누릅니다")
  @PostMapping("/avatar-posts/{postId}/likes")
  public ResponseEntity<ApiResponse<LikeCountResponse>> likeAvatarPost(@PathVariable Long postId) {
    User user = userService.getCurrentUser();
    int likeCount = likeService.addLike(user.getId(), postId, "AVATAR_POST");
    return ResponseEntity.ok(ApiResponse.success(new LikeCountResponse(likeCount)));
  }

  @Operation(summary = "아바타 포스트 좋아요 취소", description = "다른 유저의 아바타 포스트에 누른 좋아요를 취소합니다")
  @DeleteMapping("/avatar-posts/{postId}/likes")
  public ResponseEntity<ApiResponse<LikeCountResponse>> unlikeAvatarPost(
      @PathVariable Long postId) {
    User user = userService.getCurrentUser();
    int likeCount = likeService.removeLike(user.getId(), postId, "AVATAR_POST");
    return ResponseEntity.ok(ApiResponse.success(new LikeCountResponse(likeCount)));
  }
}
