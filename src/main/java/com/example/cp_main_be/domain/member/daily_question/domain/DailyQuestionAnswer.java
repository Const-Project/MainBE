package com.example.cp_main_be.domain.member.daily_question.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "daily_question_answer",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "answered_date"})})
public class DailyQuestionAnswer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 500)
  private String question;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AnswerType answer;

  @Column(name = "answered_date", nullable = false)
  private LocalDate answeredDate;

  @Builder
  public DailyQuestionAnswer(
      User user, String question, AnswerType answer, LocalDate answeredDate) {
    this.user = user;
    this.question = question;
    this.answer = answer;
    this.answeredDate = answeredDate;
  }

  public void setAnswer(Integer answer) {
    if (answer == 1) this.answer = AnswerType.YES;
    else if (answer == 2) this.answer = AnswerType.NEUTRAL;
    else this.answer = AnswerType.NO;
  }
}
