package com.example.cp_main_be.domain.mission.calendar.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class CalendarResponse {
  private int year;
  private int month;
  private List<CalendarDayResponse> days;

  public CalendarResponse(int year, int month, List<CalendarDayResponse> days) {
    this.year = year;
    this.month = month;
    this.days = days;
  }
}
