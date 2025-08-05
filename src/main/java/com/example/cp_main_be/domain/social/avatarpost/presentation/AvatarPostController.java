package com.example.cp_main_be.domain.social.avatarpost.presentation;

import com.example.cp_main_be.domain.social.avatarpost.dto.response.PostInfoResponse;
import com.example.cp_main_be.domain.social.avatarpost.service.AvatarPostService;
import com.example.cp_main_be.domain.social.bookmark.domain.repository.BookmarkRepository;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.util.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

  @GetMapping("/{postId}")
  public ResponseEntity<ApiResponse<PostInfoResponse>> getPostInfo(@PathVariable Long postId) {
    String uuidString =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    UUID userUuid = UUID.fromString(uuidString);
    User currentUser =
        userRepository
            .findByUuid(userUuid)
            .orElseThrow(() -> new IllegalArgumentException("현재 로그인한 사용자를 찾을 수 없습니다."));

    PostInfoResponse response =
        avatarPostService.getPostInfoWithBookmarkStatus(postId, currentUser);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
