package com.example.cp_main_be.domain.realquiz.dto.response;

import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealQuizAnswerResponseDTO {

  private String quizQuestion;
  private QuizType quizType;
  private Integer selectedOptionNumber; // 사용자가 선택한 답안 번호 추가
  private Integer answerNumber;
  private String answerDescription;
  private Boolean isCorrect; // 사용자 답안 정답 여부
  private Boolean isCompleted;
}
