package com.example.cp_main_be.global.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

/** 도메인과 관련된 시간 계산을 처리하는 유틸리티 클래스입니다. */
public final class TimeUtil {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private TimeUtil() {
    // 유틸리티 클래스는 인스턴스화할 수 없습니다.
  }

  /**
   * 물주기 주기의 시작 시간을 계산합니다. (매일 정오 12시 초기화)
   *
   * @return 현재 물주기 주기의 시작 LocalDateTime
   */
  public static LocalDateTime getStartOfCurrentWateringDay() {
    LocalDateTime now = LocalDateTime.now(KST);
    LocalDateTime todayNoon = now.toLocalDate().atTime(12, 0);

    if (now.isBefore(todayNoon)) {
      return todayNoon.minusDays(1); // 아직 정오가 안 지났으면, 어제 정오가 시작 시간
    } else {
      return todayNoon; // 정오가 지났으면, 오늘 정오가 시작 시간
    }
  }

  // 향후 햇빛주기 등 다른 시간 관련 로직도 여기에 추가할 수 있습니다.
  // public static LocalDateTime getStartOfCurrentSunlightDay() { ... }
}
