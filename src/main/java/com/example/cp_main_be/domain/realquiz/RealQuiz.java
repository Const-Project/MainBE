package com.example.cp_main_be.domain.realquiz;

import com.example.cp_main_be.domain.mission.quiz.enums.QuizType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RealQuiz {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "quiz_question")
  private String quizQuestion;

  @Column(name = "quiz_type")
  private QuizType quizType;

  @Column(name = "answer_number")
  private Integer answerNumber;

  @Column(name = "answer_description")
  private String answerDescription;

  @Column(name = "reward_point")
  private Long rewardPoints;

  @Column private Boolean isCompleted;

  @Column @CreationTimestamp private LocalDateTime createdAt;

}
