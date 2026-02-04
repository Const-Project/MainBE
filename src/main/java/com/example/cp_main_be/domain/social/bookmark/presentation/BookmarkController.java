package com.example.cp_main_be.domain.social.bookmark.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.avatarpost.dto.AvatarPostFeedItemResponse;
import com.example.cp_main_be.domain.social.bookmark.service.BookmarkService;
import com.example.cp_main_be.global.common.ApiResponse;
import com.example.cp_main_be.global.common.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookmarks/avatar-posts")
@Tag(name = "북마크 API", description = "아바타 포스트 북마크 기능을 제공합니다.")
public class BookmarkController {

  private final BookmarkService bookmarkService;

  @Operation(summary = "북마크 목록 조회", description = "내가 북마크한 아바타 포스트 목록을 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<List<AvatarPostFeedItemResponse>>> getMyBookmarks(
      @AuthenticationPrincipal User user,
      @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
      @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(
        ApiResponse.success(bookmarkService.getMyBookmarks(user.getId(), page, size)));
  }

  @Operation(summary = "아바타 포스트 북마크 추가", description = "아바타 포스트를 북마크합니다.")
  @PostMapping("/{postId}")
  public ResponseEntity<ApiResponse<Void>> addBookmark(
      @AuthenticationPrincipal User user, @PathVariable Long postId) {
    bookmarkService.addBookmark(user.getId(), postId);
    return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.CREATE_SUCCESS, null));
  }

  @Operation(summary = "아바타 포스트 북마크 해제", description = "아바타 포스트 북마크를 해제합니다.")
  @DeleteMapping("/{postId}")
  public ResponseEntity<ApiResponse<Void>> removeBookmark(
      @AuthenticationPrincipal User user, @PathVariable Long postId) {
    bookmarkService.removeBookmark(user.getId(), postId);
    return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.DELETE_SUCCESS, null));
  }
}
