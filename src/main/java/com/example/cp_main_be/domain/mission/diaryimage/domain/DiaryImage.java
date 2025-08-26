package com.example.cp_main_be.domain.mission.diaryimage.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiaryImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "image_url", nullable = false)
  private String imageUrl;

  @OneToOne
  @JoinColumn(name = "diary_id", unique = true)
  private Diary diary;

  // 어떤 사용자가 업로드했는지 기록하여, 추후 권한 검증에 사용합니다.
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  public void updateImageUrl(String newImageUrl) {
    this.imageUrl = newImageUrl;
  }

  public void setDiary(Diary diary) {
    this.diary = diary;
  }
}
