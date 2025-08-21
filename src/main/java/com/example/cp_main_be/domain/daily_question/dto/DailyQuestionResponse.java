package com.example.cp_main_be.domain.daily_question.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DailyQuestionResponse {
  private final String question;
}
