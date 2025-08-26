package com.example.cp_main_be.domain.mission.diary.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.diaryimage.domain.DiaryImage;
import com.example.cp_main_be.domain.social.comment.domain.Comment;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

  @OneToOne(mappedBy = "diary", fetch = FetchType.LAZY)
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

  @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference // 2. 순환 참조 방지를 위해 ManagedReference 사용
  @Builder.Default
  private List<Comment> comments = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonBackReference
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
    if (diaryImage != null) {
      diaryImage.setDiary(this); // 자식(DiaryImage)에게 부모(Diary)가 누구인지 알려줌
    }
  }

  public void increaseLikeCount() {
    this.likeCount++;
  }

  public void decreaseLikeCount() {
    this.likeCount = Math.max(0, this.likeCount - 1);
  }

  public void addComment(Comment comment) {
    this.comments.add(comment);
    comment.setDiary(this); // Comment 엔티티에 setDiary 메서드가 있다고 가정
  }
}
