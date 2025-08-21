package com.example.cp_main_be.domain.social.follow.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
// UserService import 추가
import com.example.cp_main_be.domain.social.follow.service.FollowService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "팔로우 API", description = "팔로우 관련 기능을 제공합니다.")
public class FollowController {

  private final FollowService followService;

  @Operation(summary = "팔로우", description = "다른 유저를 팔로우합니다")
  @PostMapping("/{userId}/follow")
  public ResponseEntity<ApiResponse<Void>> followUser(
      @AuthenticationPrincipal User follower, @PathVariable Long userId) {
    followService.followUser(follower.getId(), userId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "언팔로우", description = "유저를 팔로우 목록에서 삭제합니다")
  @DeleteMapping("/{userId}/follow")
  public ResponseEntity<ApiResponse<Void>> unfollowUser(
      @AuthenticationPrincipal User follower, @PathVariable Long userId) {
    followService.unfollowUser(follower.getId(), userId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "내 팔로워 조회", description = "나를 팔로우하는 유저 목록을 조회합니다")
  @GetMapping("/{userId}/followers")
  public ResponseEntity<ApiResponse<List<User>>> getFollowers(@PathVariable Long userId) {
    List<User> followers = followService.getFollowers(userId);
    return ResponseEntity.ok(ApiResponse.success(followers));
  }

  @Operation(summary = "내 팔로잉 조회", description = "내가 팔로우하는 유저 목록을 조회합니다")
  @GetMapping("/{userId}/following")
  public ResponseEntity<ApiResponse<List<User>>> getFollowing(@PathVariable Long userId) {
    List<User> following = followService.getFollowing(userId);
    return ResponseEntity.ok(ApiResponse.success(following));
  }
}
