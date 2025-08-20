package com.example.cp_main_be.domain.social.diary.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.comment.domain.Comment;
import com.example.cp_main_be.domain.social.diaryimage.domain.DiaryImage;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "diaries")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Diary {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "diary_id")
  private Long id;

  @Column(nullable = false)
  private String title;

  @Lob // TEXT 타입을 위해 사용
  @Column(nullable = false)
  private String content;

  private String keyword;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "diary_image_id")
  private DiaryImage diaryImage;

  @Column(name = "is_public")
  @Builder.Default
  private boolean isPublic = true; // 기본값 true

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "like_count")
  @Builder.Default
  private int likeCount = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "comment_id")
  private Comment comment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  // 사진은 업데이트 포함 안함
  public void updateDiary(String title, String content, boolean isPublic) {
    this.title = title;
    this.content = content;
    this.isPublic = isPublic;
  }

  public void updateImage(DiaryImage diaryImage) {
    this.diaryImage = diaryImage;
  }

  public void increaseLikeCount() {
    this.likeCount++;
  }

  public void decreaseLikeCount() {
    this.likeCount = Math.max(0, this.likeCount - 1);
  }
}
