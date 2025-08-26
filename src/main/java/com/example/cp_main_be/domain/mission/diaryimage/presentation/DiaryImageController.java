package com.example.cp_main_be.domain.mission.diaryimage.presentation;

import com.example.cp_main_be.domain.mission.diary.service.DiaryService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
@Tag(name = "일기 이미지 API", description = "일기의 이미지 관련 기능을 제공합니다")
public class DiaryImageController {

  private final DiaryService diaryService;

  @Operation(summary = "일기 이미지 등록", description = "일기 이미지를 등록합니다")
  @PostMapping(value = "{diaryId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<Void>> saveDiaryImage(
      @PathVariable Long diaryId, @RequestParam MultipartFile file) {

    // 1. 서비스 계층에 이미지 저장 및 연결 요청
    diaryService.saveDiaryImage(diaryId, file);

    // 2. 성공 응답 반환
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @Operation(summary = "일기 이미지 삭제", description = "일기 이미지를 삭제합니다")
  @DeleteMapping("/diaries/{diaryId}/images/{imageId}")
  public ResponseEntity<ApiResponse<Void>> deleteDiaryImage(
      @PathVariable Long diaryId, @PathVariable Long imageId) {
    diaryService.deleteDiaryImage(diaryId, imageId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
