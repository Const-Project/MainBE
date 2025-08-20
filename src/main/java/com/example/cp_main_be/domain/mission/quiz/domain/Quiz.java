package com.example.cp_main_be.domain.mission.quiz.domain;

import com.example.cp_main_be.domain.mission.daily_mission_master.domain.DailyMissionMaster;
import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Quiz {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "quiz_question")
  private String quizQuestion;

  @Column(name = "quiz_type")
  private QuizType quizType;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mission_master_id")
  private DailyMissionMaster dailyMissionMaster;
}
