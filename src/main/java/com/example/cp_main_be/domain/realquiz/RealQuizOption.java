package com.example.cp_main_be.domain.realquiz;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RealQuizOption {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "quiz_option_id")
  private Long id;

  @Column(name = "option_text")
  private String optionText;

  @Column(name = "option_order")
  private int optionOrder;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "quiz_id")
  private RealQuiz realQuiz;
}
