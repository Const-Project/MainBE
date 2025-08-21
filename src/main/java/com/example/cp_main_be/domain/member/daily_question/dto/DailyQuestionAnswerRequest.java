package com.example.cp_main_be.domain.member.daily_question.dto;

import com.example.cp_main_be.domain.member.daily_question.domain.AnswerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DailyQuestionAnswerRequest {

  @NotBlank(message = "Question cannot be blank")
  private String question;

  @NotNull(message = "Answer cannot be null")
  private AnswerType answer;
}
