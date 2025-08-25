package com.example.cp_main_be.domain.mission.quiz.dto;

import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompletedQuizResponseDTO {
  private final String quizQuestion;
  private final QuizType quizType;
  private final Long missionId;
  private final List<CompletedQuizOptionResponseDTO> quizOptions;
  private final Boolean isCorrect; // 사용자 답안 정답 여부
  private final Integer selectedAnswerNumber; // 사용자가 선택한 답안 번호 추가

  @Getter
  @Builder
  public static class CompletedQuizOptionResponseDTO {
    private final Long id;
    private final String text;
    private final Boolean isAnswer;
    private final Boolean isSelected; // 사용자가 선택한 옵션인지
    private final Integer optionOrder; // 옵션 순서 (1, 2, 3, 4 등)
  }
}
