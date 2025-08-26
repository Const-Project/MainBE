package com.example.cp_main_be.domain.realquiz.dto.response;

import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import com.example.cp_main_be.domain.realquiz.RealQuizOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealQuizResponseDTO {
    private String quizQuestion;
    private QuizType quizType;
    private Integer answerNumber;
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
