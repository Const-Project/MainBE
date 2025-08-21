package com.example.cp_main_be.domain.mission.user_daily_mission.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.JOINED) // 1. 상속 전략 설정 (조인 전략)
@DiscriminatorColumn(name = "mission_type") // 2. 타입을 구분할 컬럼
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public abstract class UserDailyMission { // 3. 추상 클래스로 변경
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_mission_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mission_master_id", nullable = false)
  private DailyMissionMaster dailyMissionMaster;

  @Column(name = "is_completed", nullable = false)
  private boolean isCompleted = false;

  @Column(name = "score")
  private Long score;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  // 생성자에서 필수 필드를 받도록 수정
  public UserDailyMission(User user, DailyMissionMaster dailyMissionMaster) {
    this.user = user;
    this.dailyMissionMaster = dailyMissionMaster;
  }
}
