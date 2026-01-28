package com.example.cp_main_be.domain.tracking.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrackingReportResponse {
  private String trackingType;
  private int totalScore;
  private long praiseDayCount;
  private String message; // [추가] 사용자에게 보여줄 메시지 (격려/칭찬)
}
