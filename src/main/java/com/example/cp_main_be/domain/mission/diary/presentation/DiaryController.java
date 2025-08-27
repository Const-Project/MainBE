package com.example.cp_main_be.domain.mission.diary.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import com.example.cp_main_be.domain.mission.diary.dto.request.CreateDiaryRequest;
import com.example.cp_main_be.domain.mission.diary.dto.request.UpdateDiaryRequest;
import com.example.cp_main_be.domain.mission.diary.dto.response.DiaryInfoResponse;
import com.example.cp_main_be.domain.mission.diary.dto.response.DiaryResponse;
import com.example.cp_main_be.domain.mission.diary.service.DiaryService;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/diaries")
@RequiredArgsConstructor
@Tag(name = "일기 API", description = "일기 관련 기능을 제공합니다.")
public class DiaryController {

  private final DiaryService diaryService;

  @Operation(summary = "일기 작성", description = "일기를 작성합니다")
  @PostMapping
  public ResponseEntity<ApiResponse<DiaryResponse>> createDiary(
      @AuthenticationPrincipal User user, @RequestBody @Valid CreateDiaryRequest request) {
    Long diaryId = diaryService.createDiary(user, request);
    Diary diary = diaryService.findDiaryById(diaryId);
    return ResponseEntity.ok(ApiResponse.success(DiaryResponse.from(diary)));
  }

  @Operation(summary = "내 일기 목록 조회", description = "내 일기 목록을 조회합니다")
  @GetMapping
  public ResponseEntity<ApiResponse<List<DiaryResponse>>> getMyDiaries(
      @AuthenticationPrincipal User user, @RequestParam int year, @RequestParam int month) {
    List<Diary> diaries = diaryService.findMyDiaries(user, year, month);
    List<DiaryResponse> responses = diaries.stream().map(DiaryResponse::from).toList();
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @Operation(summary = "특정 일기 조회", description = "특정 id로 일기를 조회합니다")
  @GetMapping("/{diaryId}")
  public ResponseEntity<ApiResponse<DiaryInfoResponse>> getDiaryDetail(
      @PathVariable Long diaryId, @AuthenticationPrincipal User user) {
    DiaryInfoResponse diaryInfo = diaryService.getDiaryInfo(diaryId, user);
    // TODO: 비공개 글일 경우 작성자만 볼 수 있도록 하는 로직 추가 필요
    return ResponseEntity.ok(ApiResponse.success(diaryInfo));
  }

  @Operation(summary = "일기 수정", description = "일기를 수정합니다")
  @PutMapping("/{diaryId}")
  public ResponseEntity<ApiResponse<DiaryResponse>> updateDiary(
      @AuthenticationPrincipal User user,
      @PathVariable Long diaryId,
      @RequestBody @Valid UpdateDiaryRequest request) {
    Diary updatedDiary = diaryService.updateDiary(user.getId(), diaryId, request);
    return ResponseEntity.ok(ApiResponse.success(DiaryResponse.from(updatedDiary)));
  }

  @Operation(summary = "일기 삭제", description = "일기를 삭제합니다")
  @DeleteMapping("/{diaryId}")
  public ResponseEntity<ApiResponse<Void>> deleteDiary(
      @AuthenticationPrincipal User user, @PathVariable Long diaryId) {
    diaryService.deleteDiary(user.getId(), diaryId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
