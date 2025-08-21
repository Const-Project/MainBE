package com.example.cp_main_be.domain.mission.keyword.presentation;

import com.example.cp_main_be.domain.mission.keyword.dto.response.KeywordResponse;
import com.example.cp_main_be.domain.mission.keyword.dto.response.TodayKeywordResponse;
import com.example.cp_main_be.domain.mission.keyword.service.KeywordService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/keywords")
@Tag(name = "키워드 API", description = "키워드 관련 기능을 제공합니다.")
public class KeywordController {

  private final KeywordService keywordService;

  @Operation(summary = "오늘의 일일미션 조회", description = "오늘 할당된 일일미션을 조회합니다")
  @GetMapping("/today")
  public ResponseEntity<ApiResponse<TodayKeywordResponse>> getTodaysKeywords() {
    TodayKeywordResponse response = keywordService.getTodayKeyword();
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "키워드 전부 조회", description = "모든 키워드들을 조회합니다")
  @GetMapping
  public ResponseEntity<ApiResponse<List<KeywordResponse>>> getAllKeywords() {
    List<KeywordResponse> response = keywordService.getAllKeywords();
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
