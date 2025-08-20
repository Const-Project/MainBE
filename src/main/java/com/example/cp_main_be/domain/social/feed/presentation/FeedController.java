package com.example.cp_main_be.domain.social.feed.presentation;

import com.example.cp_main_be.domain.social.feed.service.FeedService;
import com.example.cp_main_be.global.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/feed")
@Tag(name = "인증 API", description = "인증 관련 기능을 제공합니다.")
public class FeedController {

  private final FeedService feedService;

  @Operation(summary = "피드 조회", description = "통합 피드를 불러옵니다")
  @GetMapping
  public ResponseEntity<ApiResponse<List<Object>>> getFeed(
      @RequestParam(required = false) String filter) {
    String userUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    // TODO: filter 파라미터에 따라 팔로우한 사용자의 게시물만 필터링하는 로직 추가
    List<Object> feedItems = feedService.getFeed(UUID.fromString(userUuid), filter);
    return ResponseEntity.ok(ApiResponse.success(feedItems));
  }
}
