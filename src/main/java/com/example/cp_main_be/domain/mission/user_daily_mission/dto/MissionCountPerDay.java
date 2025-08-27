package com.example.cp_main_be.domain.mission.user_daily_mission.dto;

import lombok.Getter;

@Getter
public class MissionCountPerDay {
  private final int day;
  private final long count;

  public MissionCountPerDay(int day, long count) {
    this.day = day;
    this.count = count;
  }
}
