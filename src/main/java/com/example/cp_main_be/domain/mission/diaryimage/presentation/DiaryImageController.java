package com.example.cp_main_be.domain.mission.diaryimage.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.diaryimage.dto.response.ImageUploadResponse;
import com.example.cp_main_be.domain.mission.diaryimage.service.DiaryImageService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "일기 이미지 API", description = "일기의 이미지 관련 기능을 제공합니다")
public class DiaryImageController {

  // DiaryService가 아닌 DiaryImageService를 주입받아 역할을 분리합니다.
  private final DiaryImageService diaryImageService;

  @Operation(
      summary = "일기 이미지 임시 업로드",
      description = "일기 생성 전에 이미지를 먼저 업로드하고, 이미지 ID와 URL을 반환받습니다.")
  @PostMapping(value = "/diaries/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadDiaryImage(
      @RequestParam MultipartFile file, @AuthenticationPrincipal User user) {

    // 1. 서비스 계층에 이미지 업로드 요청 (아직 일기와 연결되지 않음)
    ImageUploadResponse response = diaryImageService.uploadDiaryImage(file, user);

    // 2. 업로드된 이미지의 ID와 URL을 반환
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "일기 이미지 삭제", description = "일기 이미지를 삭제합니다")
  @DeleteMapping("/diaries/{diaryId}/images/{imageId}")
  public ResponseEntity<ApiResponse<Void>> deleteDiaryImage(
      @PathVariable Long diaryId, @PathVariable Long imageId, @AuthenticationPrincipal User user) {
    diaryImageService.deleteDiaryImage(diaryId, imageId, user.getId());
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
