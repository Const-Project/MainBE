package com.example.cp_main_be.domain.mission.user_daily_mission.domain;

import com.example.cp_main_be.domain.content.image.DailyMissionImage;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDailyMission {
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

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "mission_master_id")
  DailyMissionMaster dailyMissionMaster;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @OneToOne
  @JoinColumn(name = "image_id")
  private DailyMissionImage dailyMissionImage;
}
