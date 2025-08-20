package com.example.cp_main_be.domain.social.like.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "likes",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_likes_user_target",
            columnNames = {"user_id", "target_id", "target_type"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "target_id", nullable = false)
  private Long targetId;

  @Column(name = "target_type", nullable = false)
  private String targetType; // 예: "DIARY", "AVATAR_POST"

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}
