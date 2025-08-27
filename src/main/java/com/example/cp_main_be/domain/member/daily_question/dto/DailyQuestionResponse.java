package com.example.cp_main_be.domain.member.daily_question.dto;

import lombok.Getter;

@Getter
public class DailyQuestionResponse {
  private final Long id;
  private final String question;
  private final boolean isAnswered;

  public DailyQuestionResponse(Long id, String question, boolean isAnswered) {
    this.id = id;
    this.question = question;
    this.isAnswered = isAnswered;
  }
}
