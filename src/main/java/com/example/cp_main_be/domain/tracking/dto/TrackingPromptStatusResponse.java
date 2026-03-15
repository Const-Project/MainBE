package com.example.cp_main_be.domain.tracking.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrackingPromptStatusResponse {

  // 한글 주석:
  // 홈 자동 노출 여부 판단에 필요한 값을 서버에서 한 번에 내려주기 위한 응답 DTO다.
  private boolean eligible;
  private boolean alreadyViewed;
  private long perfectDayCount;
  private String cycleKey;
  private String windowStart;
  private String windowEnd;
  private String message;
}
