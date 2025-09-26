package com.example.cp_main_be.domain.social.feed.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.feed.dto.response.FeedResponse;
import com.example.cp_main_be.domain.social.feed.service.FeedService;
import com.example.cp_main_be.global.common.ApiResponse;
import com.example.cp_main_be.global.dto.FeedItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/feed")
@Tag(name = "피드 API", description = "피드 관련 기능을 제공합니다.")
public class FeedController {

  private final FeedService feedService;

  @Operation(summary = "피드 조회", description = "통합 피드를 불러옵니다")
  @GetMapping
  public ResponseEntity<ApiResponse<List<FeedResponse>>> getFeed(
      @AuthenticationPrincipal User user, // 인증된 사용자 정보
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime cursor,
      @RequestParam(defaultValue = "20") int size) {

    // 서비스 메서드에 현재 사용자의 UUID와 필터 값을 전달
    List<FeedResponse> feedItems = feedService.getFeed(user.getUuid(), filter, cursor, size);

    // List<Object>가 아닌 List<FeedItemResponse>로 응답 타입을 명확히 합니다.
    return ResponseEntity.ok(ApiResponse.success(feedItems));
  }

  @Operation(summary = "랜덤 피드 조회 (무한 스크롤용)", description = "상세보기 화면에서 하단에 표시될 랜덤 피드를 불러옵니다")
  @GetMapping("/random")
  public ResponseEntity<ApiResponse<List<FeedItemResponse>>> getRandomFeed(
      @AuthenticationPrincipal User user,
      @RequestParam Long excludePostId, // 화면 상단에 고정된 게시물 ID (중복 방지용)
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    List<FeedItemResponse> feedItems = feedService.getRandomFeed(excludePostId, page, size);
    return ResponseEntity.ok(ApiResponse.success(feedItems));
  }
}
