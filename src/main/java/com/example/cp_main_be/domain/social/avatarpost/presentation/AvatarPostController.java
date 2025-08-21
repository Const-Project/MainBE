package com.example.cp_main_be.domain.social.avatarpost.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.domain.social.avatarpost.dto.response.PostInfoResponse;
import com.example.cp_main_be.domain.social.avatarpost.service.AvatarPostService;
import com.example.cp_main_be.domain.social.bookmark.domain.repository.BookmarkRepository;
import com.example.cp_main_be.global.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/avatar-posts")
public class AvatarPostController {

  private final AvatarPostService avatarPostService;
  private final UserRepository userRepository;
  private final BookmarkRepository bookmarkRepository;
  private final UserService userService;

  @Operation(summary = "아바타 포스트 정보 조회", description = "아바타 포스트 정보를 조회합니다")
  @GetMapping("/{postId}")
  public ResponseEntity<ApiResponse<PostInfoResponse>> getPostInfo(@PathVariable Long postId) {
    User currentUser = userService.getCurrentUser();
    PostInfoResponse response =
        avatarPostService.getPostInfoWithBookmarkStatus(postId, currentUser);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
