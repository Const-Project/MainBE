package com.example.cp_main_be.domain.admin.dto;

import com.example.cp_main_be.domain.daily_mission_masters.MissionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


public class AdminRequestDTO {

    @Getter
    @Builder
    public static class CreateMissionRequestDTO {
        @NotNull
        private MissionType missionType;

        @NotNull
        private String title;

        @NotNull
        private String description;

        @NotNull
        private String content;

        @NotNull
        private Long rewardPoints;

        private Boolean isActive;
    }

    @Getter
    @Builder
    public static class UpdateMissionRequestDTO {
        private MissionType missionType;
        private String title;
        private String description;
        private String content;
        private Long rewardPoints;
        private Boolean isActive;
    }

    @Getter
    @Builder
    public static class CreateKeywordRequestDTO {
        private String keyword;
        private LocalDateTime keywordDate;
    }
}
