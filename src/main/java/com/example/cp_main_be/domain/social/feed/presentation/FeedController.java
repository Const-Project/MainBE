package com.example.cp_main_be.domain.social.feed.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.feed.dto.request.RandomFeedRequest;
import com.example.cp_main_be.domain.social.feed.dto.request.RandomFeedSessionNextRequest;
import com.example.cp_main_be.domain.social.feed.dto.request.RandomFeedSessionStartRequest;
import com.example.cp_main_be.domain.social.feed.dto.response.FeedResponse;
import com.example.cp_main_be.domain.social.feed.dto.response.FeedScrollResponse;
import com.example.cp_main_be.domain.social.feed.dto.response.RandomFeedSessionResponse;
import com.example.cp_main_be.domain.social.feed.service.FeedService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
  @PostMapping("/random") // GET → POST로 변경
  public ResponseEntity<ApiResponse<FeedScrollResponse>> getRandomFeed(
      @AuthenticationPrincipal User user,
      @RequestBody RandomFeedRequest request) { // @RequestBody로 변경

    FeedScrollResponse response =
        feedService.getRandomFeed(
            user.getUuid(),
            request.getExcludeDiaryIds() != null
                ? request.getExcludeDiaryIds()
                : Collections.emptyList(),
            request.getExcludeAvatarPostIds() != null
                ? request.getExcludeAvatarPostIds()
                : Collections.emptyList(),
            request.getSize());

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "랜덤 피드 세션 시작", description = "랜덤 피드 세션을 시작합니다")
  @PostMapping("/random/session")
  public ResponseEntity<ApiResponse<RandomFeedSessionResponse>> startRandomFeedSession(
      @AuthenticationPrincipal User user, @RequestBody RandomFeedSessionStartRequest request) {
    RandomFeedSessionResponse response =
        feedService.startRandomFeedSession(user.getUuid(), request.getSize());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "랜덤 피드 세션 다음 페이지", description = "랜덤 피드 세션의 다음 페이지를 불러옵니다")
  @PostMapping("/random/next")
  public ResponseEntity<ApiResponse<RandomFeedSessionResponse>> getRandomFeedSessionNext(
      @RequestBody RandomFeedSessionNextRequest request) {
    RandomFeedSessionResponse response =
        feedService.getRandomFeedSessionNext(request.getSessionToken(), request.getSize());
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
