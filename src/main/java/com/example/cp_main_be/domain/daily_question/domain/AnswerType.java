package com.example.cp_main_be.domain.daily_question.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnswerType {
  YES("그렇다"),
  NEUTRAL("보통이다"),
  NO("아니다");

  private final String description;
}
