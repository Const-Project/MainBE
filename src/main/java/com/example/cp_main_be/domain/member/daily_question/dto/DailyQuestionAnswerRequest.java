package com.example.cp_main_be.domain.member.daily_question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DailyQuestionAnswerRequest {

  @NotBlank(message = "Question cannot be blank")
  private Long questionId;

  @NotNull(message = "Answer cannot be null")
  private Integer answer;
}
