package com.example.cp_main_be.domain.home;

import com.example.cp_main_be.domain.home.service.HomeService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/home")
@Tag(name = "홈 API", description = "홈 화면 관련 기능을 제공합니다.")
public class HomeController {

  private final HomeService homeService;

  @Operation(summary = "홈 화면 데이터 조회", description = "사용자의 홈 화면에 필요한 모든 정보를 한번에 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<HomeResponseDto>> getHomeScreenData(
      @AuthenticationPrincipal User user) {
    HomeResponseDto response = homeService.getHomeScreenData(user.getId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
