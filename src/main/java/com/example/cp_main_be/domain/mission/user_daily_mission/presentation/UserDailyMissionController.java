package com.example.cp_main_be.domain.mission.user_daily_mission.presentation;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.daily_mission_master.dto.response.DailyMissionResponseDTO;
import com.example.cp_main_be.domain.mission.quiz.dto.CompletedQuizResponseDTO;
import com.example.cp_main_be.domain.mission.quiz.dto.QuizRequestDTO;
import com.example.cp_main_be.domain.mission.quiz.dto.QuizResponseDTO;
import com.example.cp_main_be.domain.mission.quiz.service.QuizService;
import com.example.cp_main_be.domain.mission.user_daily_mission.service.UserDailyMissionService;
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
@RequestMapping("/api/v1/mission")
@RequiredArgsConstructor
@Tag(name = "일일미션 API", description = "일일미션 관련 기능을 제공합니다.")
public class UserDailyMissionController {

  private final UserDailyMissionService userDailyMissionService;
  private final QuizService quizService;

  @GetMapping("/daily")
  @Operation(summary = "유저 일일 미션 목록 조회")
  public ResponseEntity<ApiResponse<DailyMissionResponseDTO>> getDailyMissions(
      @AuthenticationPrincipal User user) {
    DailyMissionResponseDTO response = userDailyMissionService.getDailyMissions(user.getId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  // 미션 할당 API도 필요하다 .

  @PostMapping("/daily/{userDailyMissionId}/complete")
  @Operation(summary = "미션 완료 처리 API")
  public ResponseEntity<ApiResponse<Void>> completeDailyMission(
      @PathVariable Long userDailyMissionId) {
    userDailyMissionService.completeDailyMission(userDailyMissionId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @GetMapping("/quiz/{userDailyMissionId}")
  @Operation(summary = "퀴즈 문제 조회 API")
  public ResponseEntity<ApiResponse<QuizResponseDTO>> getQuiz(
      @PathVariable(name = "userDailyMissionId") Long userDailyMissionId) {

    QuizResponseDTO result = quizService.getQuiz(userDailyMissionId);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  // 새로운 엔드포인트 - 완료된 퀴즈 결과 조회
  @GetMapping("/quiz/{userDailyMissionId}/result")
  @Operation(summary = "완료된 퀴즈 결과 조회 API (정답 정보 포함)")
  public ResponseEntity<ApiResponse<CompletedQuizResponseDTO>> getQuizResult(
      @PathVariable(name = "userDailyMissionId") Long userDailyMissionId) {
    CompletedQuizResponseDTO result = quizService.getCompletedQuizResult(userDailyMissionId);
    return ResponseEntity.ok(ApiResponse.success(result));
  }

  @PostMapping(
      value = "/photo/{userDailyMissionId}/upload",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "미션 사진 업로드 API")
  public ResponseEntity<ApiResponse<String>> uploadPictureForMission(
      @RequestParam MultipartFile file,
      @PathVariable(name = "userDailyMissionId") Long userDailyMissionId) {
    String imageUrl =
        userDailyMissionService.uploadPictureForDailyMission(userDailyMissionId, file);
    return ResponseEntity.ok(ApiResponse.success(imageUrl));
  }

  @PostMapping("/quiz/{userDailyMissionId}/answer")
  @Operation(summary = "퀴즈 답변 제출 API")
  public ResponseEntity<ApiResponse<Boolean>> answerQuiz(
      @PathVariable(name = "userDailyMissionId") Long userDailyMissionId,
      @RequestBody QuizRequestDTO request) {
    Boolean result = userDailyMissionService.summitAnswer(request, userDailyMissionId);
    return ResponseEntity.ok(ApiResponse.success(result));
  }
}
