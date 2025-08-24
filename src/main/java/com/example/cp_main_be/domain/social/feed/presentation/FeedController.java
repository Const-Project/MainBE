package com.example.cp_main_be.domain.social.feed.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.feed.dto.response.FeedResponse;
import com.example.cp_main_be.domain.social.feed.service.FeedService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    // 서비스 메서드에 현재 사용자의 UUID와 필터 값을 전달
    List<FeedResponse> feedItems = feedService.getFeed(user.getUuid(), filter, page, size);

    // List<Object>가 아닌 List<FeedItemResponse>로 응답 타입을 명확히 합니다.
    return ResponseEntity.ok(ApiResponse.success(feedItems));
  }
}
