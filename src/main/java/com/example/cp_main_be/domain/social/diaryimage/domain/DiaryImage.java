package com.example.cp_main_be.domain.social.diaryimage.domain;

import com.example.cp_main_be.domain.social.diary.domain.Diary;
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

  @Setter
  @OneToOne
  @JoinColumn(name = "diary_id", nullable = false)
  private Diary diary;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  public void updateImageUrl(String newImageUrl) {
    this.imageUrl = newImageUrl;
  }
}
