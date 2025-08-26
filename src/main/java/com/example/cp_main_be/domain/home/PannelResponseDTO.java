package com.example.cp_main_be.domain.home;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PannelResponseDTO {
  private boolean isDairyCompleted;
  private boolean isCheckingCompleted;
  private boolean isQuizCompleted;
}
