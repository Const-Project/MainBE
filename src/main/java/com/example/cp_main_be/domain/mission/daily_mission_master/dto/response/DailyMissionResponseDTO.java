package com.example.cp_main_be.domain.mission.daily_mission_master.dto.response;

import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyMissionResponseDTO {

  List<DailyMissionResponseDTO.MissionSummaryDTO> todayMissions;

  @Getter
  @Builder
  public static class MissionSummaryDTO {
    Long missionId;
    String missionTitle;
    String missionDescription;
    Boolean isCompleted;

    public static DailyMissionResponseDTO.MissionSummaryDTO from(
        DailyMissionMaster dailyMissionMaster) {
      return MissionSummaryDTO.builder()
          .missionId(dailyMissionMaster.getId())
          .missionTitle(dailyMissionMaster.getTitle())
          .missionDescription(dailyMissionMaster.getDescription())
          .build();
    }
  }

  public static DailyMissionResponseDTO from(List<DailyMissionMaster> dailyMissionMasters) {
    List<DailyMissionResponseDTO.MissionSummaryDTO> dailyMissionResponseDTOS =
        dailyMissionMasters.stream().map(MissionSummaryDTO::from).toList();

    return DailyMissionResponseDTO.builder().todayMissions(dailyMissionResponseDTOS).build();
  }
}
