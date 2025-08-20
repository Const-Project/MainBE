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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mission_master_id")
  DailyMissionMaster dailyMissionMaster;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "selected_option_id")
  private Long selectedOptionId; // 사용자가 선택한 퀴즈 옵션 ID

  // ===== 새로 추가할 필드들 =====
  @Column(name = "selected_answer_number")
  private Integer selectedAnswerNumber; // 사용자가 선택한 답안 번호 (1,2,3,4)

  @Column(name = "is_quiz_correct")
  private Boolean isQuizCorrect; // 퀴즈 정답 여부

  @Column(name = "quiz_answered_at")
  private LocalDateTime quizAnsweredAt; // 퀴즈 답안 제출 시간

  @OneToOne
  @JoinColumn(name = "image_id")
  private DailyMissionImage dailyMissionImage;

  public void markAsCompleted() {
    this.isCompleted = true;
    this.completedAt = LocalDateTime.now();
  }

  public void markAsCompleted(Long score) {
    this.isCompleted = true;
    this.completedAt = LocalDateTime.now();
    this.score = score;
  }

  public boolean hasSubmittedQuizAnswer() {
    return this.selectedOptionId != null;
  }
}
