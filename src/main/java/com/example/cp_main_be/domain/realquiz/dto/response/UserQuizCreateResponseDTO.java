package com.example.cp_main_be.domain.realquiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQuizCreateResponseDTO {

  private Long quizId;
  private String userName;
  private Long userId;
}
