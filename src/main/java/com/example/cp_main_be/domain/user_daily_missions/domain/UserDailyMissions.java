package com.example.cp_main_be.domain.user_daily_missions.domain;

import com.example.cp_main_be.domain.daily_mission_masters.domain.DailyMissionMasters;
import com.example.cp_main_be.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDailyMissions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_mission_id")
    private Long id;

    @Column(name = "mission_date")
    private LocalDateTime missionDate;

    @Column(name = "is_completed")
    private boolean isCompleted;

    @Column(name = "score")
    private Long score;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_master_id")
    DailyMissionMasters dailyMissionMasters;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
