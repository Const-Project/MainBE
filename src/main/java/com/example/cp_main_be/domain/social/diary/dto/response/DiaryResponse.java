package com.example.cp_main_be.domain.social.diary.dto.response;

import com.example.cp_main_be.domain.social.diary.domain.Diary;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class DiaryResponse {
  private final Long diaryId;
  private final String title;
  private final String content;
  private final String imageUrl;
  private final boolean isPublic;
  private final int likeCount;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;

  // private AuthorDto author; // 작성자 정보가 필요하면 추가

  // Lombok Builder나 생성자를 통해 더 유연하게 만들 수 있습니다.
  private DiaryResponse(
      Long diaryId,
      String title,
      String content,
      String imageUrl,
      boolean isPublic,
      int likeCount,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.diaryId = diaryId;
    this.title = title;
    this.content = content;
    this.imageUrl = imageUrl;
    this.isPublic = isPublic;
    this.likeCount = likeCount;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static DiaryResponse from(Diary diary) {
    String imageUrl = diary.getDiaryImage() != null ? diary.getDiaryImage().getImageUrl() : null;
    return new DiaryResponse(
        diary.getId(),
        diary.getTitle(),
        diary.getContent(),
        imageUrl,
        diary.isPublic(),
        diary.getLikeCount(),
        diary.getCreatedAt(),
        diary.getUpdatedAt());
  }
}
