package com.example.cp_main_be.domain.quiz.dto;

import com.example.cp_main_be.domain.quiz.domain.QuizOptions;
import com.example.cp_main_be.domain.quiz.enums.QuizType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuizResponseDTO {
        String quizQuestion;
        QuizType quizType;
        Long missionId;
        List<QuizOptions> quizOptions; // 나중에 ListResponse<QuizOptionResponseDTO> 타입으로 수정
        Boolean isCompleted;
}
