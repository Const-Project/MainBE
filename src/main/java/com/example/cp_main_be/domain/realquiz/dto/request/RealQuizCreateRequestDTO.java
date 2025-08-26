package com.example.cp_main_be.domain.realquiz.dto.request;

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
public class RealQuizCreateRequestDTO {

  private String quizQuestion;
  private QuizType quizType;
  private Integer answerNumber;
  private String answerDescription;
  private List<RealQuizOptionCreateDTO> realQuizOptionList;

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RealQuizOptionCreateDTO {
    private String optionText;
    private int optionOrder;
  }
}
