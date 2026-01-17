package com.example.cp_main_be.domain.social.like.diary.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "diary_likes",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_diary_like",
            columnNames = {"user_id", "diary_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiaryLike {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "diary_id", nullable = false)
  private Diary diary;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @Builder
  public DiaryLike(User user, Diary diary) {
    this.user = user;
    this.diary = diary;
  }
}
