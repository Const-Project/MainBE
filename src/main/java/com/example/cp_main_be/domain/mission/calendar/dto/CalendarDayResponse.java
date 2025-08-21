package com.example.cp_main_be.domain.mission.calendar.dto;

import lombok.Getter;

@Getter
public class CalendarDayResponse {
  private int day;
  private int missionCompleteCount;

  public CalendarDayResponse(int day, int missionCompleteCount) {
    this.day = day;
    this.missionCompleteCount = missionCompleteCount;
  }
}
