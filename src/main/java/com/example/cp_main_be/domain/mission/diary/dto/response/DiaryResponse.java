package com.example.cp_main_be.domain.mission.diary.dto.response;

import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class DiaryResponse {
  private final Long diaryId;
  private final String title;
  private final String content;
  private final String imageUrl;
  private final boolean isPublic;
  private final long likeCount; // [수정] int -> long
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;

  private DiaryResponse(
      Long diaryId,
      String title,
      String content,
      String imageUrl,
      boolean isPublic,
      long likeCount, // [수정] int -> long
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

  // [수정] 생성자에서 likeCount를 직접 받도록 변경
  public static DiaryResponse from(Diary diary, long likeCount) {
    String imageUrl = diary.getDiaryImage() != null ? diary.getDiaryImage().getImageUrl() : null;
    return new DiaryResponse(
        diary.getId(),
        diary.getTitle(),
        diary.getContent(),
        imageUrl,
        diary.isPublic(),
        likeCount, // [수정] 파라미터로 받은 값을 사용
        diary.getCreatedAt(),
        diary.getUpdatedAt());
  }
}
