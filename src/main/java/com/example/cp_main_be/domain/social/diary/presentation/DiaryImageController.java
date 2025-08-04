package com.example.cp_main_be.domain.social.diary.presentation;

import com.example.cp_main_be.domain.social.diary.service.DiaryService;
import com.example.cp_main_be.global.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/diaries")
public class DiaryImageController {

  private final DiaryService diaryService;

  @PostMapping("{diaryId}/images")
  public ResponseEntity<ApiResponse<Void>> saveDiaryImage(
      @PathVariable Long diaryId, @RequestParam("file") MultipartFile file) {

    // 1. 서비스 계층에 이미지 저장 및 연결 요청
    diaryService.saveDiaryImage(diaryId, file);

    // 2. 성공 응답 반환
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
