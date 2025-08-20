package com.example.cp_main_be.domain.social.diary.presentation;

import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.dto.request.DiaryWriteRequest;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryIdResponse;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryResponse;
import com.example.cp_main_be.domain.social.diary.service.DiaryService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/diaries")
public class DiaryController {

  private final DiaryService diaryService;
  private final UserService userService;

  @Operation(summary = "내 모든 일기 조회", description = "유저가 작성한 모든 일기 조회")
  @GetMapping
  public ResponseEntity<ApiResponse<List<DiaryResponse>>> getDiaries() {
    String userUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    User user = userService.findUserByUuid(UUID.fromString(userUuid));
    List<DiaryResponse> diaryResponses = diaryService.findAllDiariesByUserId(user.getId());
    return ResponseEntity.ok(ApiResponse.success(diaryResponses));
  }

  @Operation(summary = "일기 등록", description = "일기를 등록합니다")
  @PostMapping
  public ResponseEntity<ApiResponse<DiaryIdResponse>> registerDiary(
      @Valid @RequestBody DiaryWriteRequest request) {
    String userUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    User user = userService.findUserByUuid(UUID.fromString(userUuid));

    Long diaryId =
        diaryService.registerDiary(
            request.getTitle(),
            request.getContent(),
            request.getKeyword(),
            user,
            request.isPublic());

    DiaryIdResponse diaryIdResponse = diaryService.getDiaryIdResponseById(diaryId);
    return ResponseEntity.ok(ApiResponse.success(diaryIdResponse));
  }

  @Operation(summary = "특정 일기 조회", description = "특정 일기를 조회합니다")
  @GetMapping("/{diaryId}")
  public ResponseEntity<ApiResponse<DiaryResponse>> getDiaryById(@PathVariable Long diaryId) {

    // id로 일기 조회할거임, 남이 적은 일기도 접근 가능한 상황
    DiaryResponse diaryById = diaryService.getDiaryResponseById(diaryId);
    return ResponseEntity.ok(ApiResponse.success(diaryById));
  }

  @Operation(summary = "특정 일기 수정", description = "특정 일기를 수정합니다")
  @PutMapping("/{diaryId}")
  public ResponseEntity<ApiResponse<DiaryResponse>> updateDiaryById(
      @PathVariable Long diaryId, @Valid @RequestBody DiaryWriteRequest request) {
    // 작성한 사람만 수정 가능
    Diary diary = diaryService.updateDiary(diaryId, request);
    DiaryResponse diaryResponse = DiaryResponse.from(diary);
    return ResponseEntity.ok(ApiResponse.success(diaryResponse));
  }

  @Operation(summary = "특정 일기 삭제", description = "특정 일기를 삭제합니다")
  @DeleteMapping("/{diaryId}")
  public ResponseEntity<ApiResponse<Void>> deleteDiaryById(@PathVariable Long diaryId) {
    // 작성한 사람만 삭제 가능
    diaryService.deleteDiaryById(diaryId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
