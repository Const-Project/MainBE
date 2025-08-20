package com.example.cp_main_be.domain.daily_mission_masters.dto.response;

import com.example.cp_main_be.domain.daily_mission_masters.domain.DailyMissionMasters;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

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
        public static DailyMissionResponseDTO.MissionSummaryDTO from(DailyMissionMasters dailyMissionMasters)
        {
            return MissionSummaryDTO.builder()
                    .missionId(dailyMissionMasters.getId())
                    .missionTitle(dailyMissionMasters.getTitle())
                    .missionDescription(dailyMissionMasters.getDescription())
                    .build();
        }
    }

    public static DailyMissionResponseDTO from(List<DailyMissionMasters> dailyMissionMasters) {
        List<DailyMissionResponseDTO.MissionSummaryDTO> dailyMissionResponseDTOS = dailyMissionMasters.stream()
                .map(MissionSummaryDTO::from)
                .toList();

        return DailyMissionResponseDTO.builder()
                .todayMissions(dailyMissionResponseDTOS)
                .build();
    }

}
