package com.example.cp_main_be.domain.tracking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TrackingPromptConfirmRequest {

  // 한글 주석:
  // 앱이 이번 14일 주기를 확인했다고 서버에 기록할 때 사용하는 요청값이다.
  private String cycleKey;
}
