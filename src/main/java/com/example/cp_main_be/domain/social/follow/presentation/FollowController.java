package com.example.cp_main_be.domain.social.follow.presentation;

import com.example.cp_main_be.domain.social.follow.service.FollowService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.service.UserService; // UserService import 추가
import com.example.cp_main_be.global.util.ApiResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class FollowController {

  private final FollowService followService;
  private final UserService userService; // UserService 주입

  @PostMapping("/{userId}/follow")
  public ResponseEntity<ApiResponse<Void>> followUser(@PathVariable Long userId) {
    String followerUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User follower = userService.findUserByUuid(UUID.fromString(followerUuid));
    followService.followUser(follower.getId(), userId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @DeleteMapping("/{userId}/follow")
  public ResponseEntity<ApiResponse<Void>> unfollowUser(@PathVariable Long userId) {
    String followerUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User follower = userService.findUserByUuid(UUID.fromString(followerUuid));
    followService.unfollowUser(follower.getId(), userId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @GetMapping("/{userId}/followers")
  public ResponseEntity<ApiResponse<List<User>>> getFollowers(@PathVariable Long userId) {
    List<User> followers = followService.getFollowers(userId);
    return ResponseEntity.ok(ApiResponse.success(followers));
  }

  @GetMapping("/{userId}/following")
  public ResponseEntity<ApiResponse<List<User>>> getFollowing(@PathVariable Long userId) {
    List<User> following = followService.getFollowing(userId);
    return ResponseEntity.ok(ApiResponse.success(following));
  }
}
