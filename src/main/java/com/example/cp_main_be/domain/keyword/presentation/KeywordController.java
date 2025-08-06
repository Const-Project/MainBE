package com.example.cp_main_be.domain.keyword.presentation;

import com.example.cp_main_be.domain.keyword.dto.response.KeywordListResponse;
import com.example.cp_main_be.domain.keyword.dto.response.TodayKeywordResponse;
import com.example.cp_main_be.domain.keyword.service.KeywordService;
import com.example.cp_main_be.global.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/keywords")
public class KeywordController {

  private final KeywordService keywordService;

  @GetMapping("/today")
  public ResponseEntity<ApiResponse<TodayKeywordResponse>> getTodaysKeywords() {
    TodayKeywordResponse response = keywordService.getTodayKeyword();
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<KeywordListResponse>> getAllKeywords() {
    KeywordListResponse response = keywordService.getAllKeywords();
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
