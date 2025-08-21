package com.example.cp_main_be.domain.social.comment.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "writer_id", nullable = false)
  private User writer;

  @Column(nullable = false)
  private String content;

  // 2. 각 부모와 명확한 연관관계를 설정 (둘 중 하나만 값이 있게 됨)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "avatar_post_id") // 컬럼 이름 명시
  @JsonBackReference // 순환 참조 방지
  private AvatarPost avatarPost;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "diary_id") // Diary와 연결될 컬럼 추가
  @JsonBackReference // 순환 참조 방지
  private Diary diary;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
