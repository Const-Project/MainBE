package com.example.cp_main_be.domain.keyword.presentation;

import com.example.cp_main_be.domain.keyword.dto.response.KeywordResponse;
import com.example.cp_main_be.domain.keyword.dto.response.TodayKeywordResponse;
import com.example.cp_main_be.domain.keyword.service.KeywordService;
import com.example.cp_main_be.global.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/keywords")
public class KeywordController {

  private final KeywordService keywordService;

  @Operation(summary = "오늘의 키워드 조회", description = "오늘의 키워드를 조회합니다.")
  @GetMapping("/today")
  public ResponseEntity<ApiResponse<TodayKeywordResponse>> getTodaysKeywords() {
    TodayKeywordResponse response = keywordService.getTodayKeyword();
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "모든 키워드 조회", description = "모든 키워드를 날짜와 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<List<KeywordResponse>>> getAllKeywords() {
    List<KeywordResponse> response = keywordService.getAllKeywords();
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
