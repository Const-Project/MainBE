package com.example.cp_main_be.domain.social.diary.dto.response;

import com.example.cp_main_be.domain.social.comment.domain.Comment;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class DiaryResponse {

  private final Long id;
  private final Long userId;
  private final String title;
  private final String content;
  private final String keyword;
  private final Comment comment;
  private final Long likeCount;
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;

  @Builder
  private DiaryResponse(
          Long id,
          Long userId,
          String title,
          String content,
          String keyword, Comment comment, Long likeCount,
          LocalDateTime createdAt,
          LocalDateTime updatedAt) {
    this.id = id;
    this.userId = userId;
    this.title = title;
    this.content = content;
    this.keyword = keyword;
    this.comment = comment;
    this.likeCount = likeCount;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static DiaryResponse from(Diary diary) {
    return DiaryResponse.builder()
        .id(diary.getId())
        .userId(diary.getUser().getId())
        .title(diary.getTitle())
        .content(diary.getContent())
        .keyword(diary.getKeyword())
        .comment(diary.getComment())
        .likeCount(diary.getLikeCount())
        .createdAt(diary.getCreatedAt())
        .updatedAt(diary.getUpdatedAt())
        .build();
  }
}
