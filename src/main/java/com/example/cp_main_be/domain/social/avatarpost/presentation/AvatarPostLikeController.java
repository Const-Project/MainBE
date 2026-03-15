package com.example.cp_main_be.domain.social.avatarpost.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.like.avatar_post.service.AvatarPostLikeService;
import com.example.cp_main_be.global.common.ApiResponse;
import com.example.cp_main_be.global.common.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/avatar-posts/{postId}/likes")
public class AvatarPostLikeController {

  private final AvatarPostLikeService avatarPostLikeService;

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> likeAvatarPost(
      @PathVariable Long postId, @AuthenticationPrincipal User user) {
    avatarPostLikeService.likeAvatarPost(user.getId(), postId);
    return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.CREATE_SUCCESS, null));
  }

  @DeleteMapping
  public ResponseEntity<ApiResponse<Void>> unlikeAvatarPost(
      @PathVariable Long postId, @AuthenticationPrincipal User user) {
    avatarPostLikeService.unlikeAvatarPost(user.getId(), postId);
    return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.DELETE_SUCCESS, null));
  }
}
