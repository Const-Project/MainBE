package com.example.cp_main_be.domain.mission.user_daily_mission.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // 1. 상속 전략 변경
@DiscriminatorColumn(name = "mission_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public abstract class UserDailyMission {
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

  // -- 사진 미션용 컬럼 --
  @Column(name = "submission_image_url", length = 2048)
  private String submissionImageUrl;

  // -- 퀴즈 미션용 컬럼 --
  @Column(name = "selected_answer_number")
  private Integer selectedAnswerNumber;

  @Column(name = "is_quiz_correct")
  private Boolean isQuizCorrect;

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
