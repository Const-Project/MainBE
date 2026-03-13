package com.example.cp_main_be.domain.realquiz;

import com.example.cp_main_be.domain.member.user.domain.User;
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
public class UserQuiz {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JoinColumn(name = "user_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @JoinColumn(name = "real_quiz_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private RealQuiz realQuiz;

  @Column @Builder.Default private Boolean isCompleted = false;

  @Column(name = "selected_option_order")
  private Integer selectedOptionOrder;

  @Column @CreationTimestamp private LocalDateTime createdAt;
}
