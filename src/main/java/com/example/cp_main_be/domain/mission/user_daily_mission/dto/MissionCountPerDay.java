package com.example.cp_main_be.domain.mission.user_daily_mission.dto;

import lombok.Getter;

@Getter
public class MissionCountPerDay {
  private int day;
  private long count;

  public MissionCountPerDay(int day, long count) {
    this.day = day;
    this.count = (int) count;
  }
}
