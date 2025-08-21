package com.example.cp_main_be.domain.mission.quiz.dto;

import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizRequestDTO {

  private QuizType quizType;
  private Long selectedOptionId;
}
