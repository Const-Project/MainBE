package com.example.cp_main_be.domain.member.daily_question.presentation;

import com.example.cp_main_be.domain.member.daily_question.dto.DailyQuestionAnswerRequest;
import com.example.cp_main_be.domain.member.daily_question.dto.DailyQuestionResponse;
import com.example.cp_main_be.domain.member.daily_question.service.DailyQuestionAnswerService;
import com.example.cp_main_be.domain.member.daily_question.service.DailyQuestionService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/survey")
@Tag(name = "오늘의 질문 API", description = "매일 다른 질문을 제공하고 답변을 저장합니다.")
public class DailyQuestionController {

  private final DailyQuestionService dailyQuestionService;
  private final DailyQuestionAnswerService dailyQuestionAnswerService;

  @Operation(
      summary = "오늘의 질문 조회",
      description = "앱 구동 시 호출하여 오늘의 질문을 가져옵니다. 사용자가 답변했는지 여부(isAnswered)도 함께 반환합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<DailyQuestionResponse>> getDailyQuestion(
      @AuthenticationPrincipal User user) {
    DailyQuestionService.QuestionInfo questionInfo = dailyQuestionService.getQuestionInfoForToday();
    boolean isAnswered = dailyQuestionAnswerService.hasUserAnsweredToday(user);
    DailyQuestionResponse response =
        new DailyQuestionResponse(questionInfo.getId(), questionInfo.getText(), isAnswered);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @Operation(summary = "오늘의 질문 답변 저장", description = "오늘의 질문에 대한 답변을 저장합니다.")
  @PostMapping("/answer")
  public ResponseEntity<ApiResponse<Void>> saveDailyQuestionAnswer(
      @AuthenticationPrincipal User user, @Valid @RequestBody DailyQuestionAnswerRequest request) {
    dailyQuestionAnswerService.saveAnswer(user, request);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
