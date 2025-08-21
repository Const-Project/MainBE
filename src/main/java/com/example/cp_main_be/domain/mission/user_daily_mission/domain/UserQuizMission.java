package com.example.cp_main_be.domain.mission.user_daily_mission.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("QUIZ") // 부모 테이블의 mission_type 컬럼에 "QUIZ"로 저장됨
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserQuizMission extends UserDailyMission {
  @Column(name = "selected_option_id")
  private Long selectedOptionId;

  @Column(name = "is_quiz_correct")
  private Boolean isQuizCorrect;

  @Column(name = "quiz_answered_at")
  private LocalDateTime quizAnsweredAt;

  @Column(name = "selected_answer_number")
  private Integer selectedAnswerNumber;

  public void setSelectedAnswerNumber(int optionOrder) {
    this.selectedAnswerNumber = optionOrder;
  }
}
