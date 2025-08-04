package com.example.cp_main_be.domain.social.diary.presentation;

import com.example.cp_main_be.domain.social.diary.dto.request.DiaryWriteRequest;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryIdResponse;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryResponse;
import com.example.cp_main_be.domain.social.diary.service.DiaryService;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.service.UserService;
import com.example.cp_main_be.global.util.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/diaries")
public class DiaryController {

  private final DiaryService diaryService;
  private final UserService userService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<DiaryResponse>>> getDiaries() {
    String userUuid =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    User user = userService.findUserByUuid(UUID.fromString(userUuid));
    List<DiaryResponse> diaryResponses = diaryService.findAllDiariesByUserId(user.getId());
    return ResponseEntity.ok(ApiResponse.success(diaryResponses));
  }

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
            request.getImageUrl(),
            request.getKeyword(),
            user);

    DiaryIdResponse diaryIdResponse = diaryService.findDiaryIdById(diaryId);
    return ResponseEntity.ok(ApiResponse.success(diaryIdResponse));
  }

  @GetMapping("/{diaryId}")
  public ResponseEntity<ApiResponse<DiaryResponse>> getDiaryById(
          @PathVariable Long diaryId) {

    // id로 일기 조회할거임, 남이 적은 일기도 접근 가능한 상황
    DiaryResponse diaryById = diaryService.findDiaryById(diaryId);
    return ResponseEntity.ok(ApiResponse.success(diaryById));
  }
}
