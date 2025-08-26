package com.example.cp_main_be.domain.realquiz.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQuizCreateRequestDTO {
    private Long quizId;
    private Long userId;
}
