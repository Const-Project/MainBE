package com.example.cp_main_be.domain.realquiz.dto.response;

import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealQuizResponseDTO {
  private Long quizId;
  private String quizQuestion;
  private QuizType quizType;
  private Boolean isCompleted;
  private Integer selectedOptionNumber;
  private Integer answerNumber;
  private Boolean isCorrect;
  private String answerDescription;
  private List<RealQuizOptionResponseDTO> quizOptions;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RealQuizOptionResponseDTO {
    private Integer optionOrder;
    private String optionText;
  }
}
