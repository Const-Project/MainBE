package com.example.cp_main_be.domain.mission;

import com.example.cp_main_be.domain.mission.daily_mission_master.MissionType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MissionStatusDto {
  private String title;
  private MissionType missionType;
  private boolean completed;
}
