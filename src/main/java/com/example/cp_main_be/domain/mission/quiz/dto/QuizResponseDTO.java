package com.example.cp_main_be.domain.mission.quiz.dto;

import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizResponseDTO {
  private final String quizQuestion;
  private final QuizType quizType;
  private final Long missionId;
  private final List<QuizOptionResponseDTO> quizOptions;
  private final Boolean isCompleted;

  @Getter
  @Builder
  public static class QuizOptionResponseDTO {
    private final Long id;
    private final String text;
    private final Boolean isAnswer;
  }
}
