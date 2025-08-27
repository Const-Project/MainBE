package com.example.cp_main_be.domain.member.daily_question.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DailyQuestionAnswerRequest {

  @NotNull(message = "질문 ID는 필수입니다.")
  private Long questionId;

  @NotNull(message = "Answer cannot be null")
  private Integer answer;
}
