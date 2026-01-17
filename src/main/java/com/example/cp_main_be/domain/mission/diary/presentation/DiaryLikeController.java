package com.example.cp_main_be.domain.mission.diary.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.like.diary.service.DiaryLikeService;
import com.example.cp_main_be.global.common.ApiResponse;
import com.example.cp_main_be.global.common.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diaries/{diaryId}/likes")
public class DiaryLikeController {

  private final DiaryLikeService diaryLikeService;

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> likeDiary(
      @PathVariable Long diaryId, @AuthenticationPrincipal User user) {
    diaryLikeService.likeDiary(user.getId(), diaryId);
    return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.CREATE_SUCCESS, null));
  }

  @DeleteMapping
  public ResponseEntity<ApiResponse<Void>> unlikeDiary(
      @PathVariable Long diaryId, @AuthenticationPrincipal User user) {
    diaryLikeService.unlikeDiary(user.getId(), diaryId);
    return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.DELETE_SUCCESS, null));
  }
}
