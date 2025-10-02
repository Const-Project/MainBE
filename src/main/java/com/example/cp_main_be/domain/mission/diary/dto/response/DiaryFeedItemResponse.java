package com.example.cp_main_be.domain.mission.diary.dto.response;

import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import com.example.cp_main_be.global.dto.AuthorResponse;
import com.example.cp_main_be.global.dto.FeedItemResponse;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class DiaryFeedItemResponse implements FeedItemResponse {
  private final String postType = "DIARY";
  private final Long postId;
  private final AuthorResponse author;
  private final String title;
  private final String content;
  private final String imageUrl;
  private final long likeCount; // [수정] int -> long 타입으로 변경 (Repository 반환 타입과 일치)
  private final long commentCount;
  private final LocalDateTime createdAt;

  // [수정] 생성자에서 likeCount와 commentCount를 직접 받도록 변경
  public DiaryFeedItemResponse(Diary diary, long likeCount, long commentCount) {
    this.postId = diary.getId();
    this.author = new AuthorResponse(diary.getUser());
    this.title = diary.getTitle();
    this.content = diary.getContent();
    this.imageUrl = diary.getDiaryImage() != null ? diary.getDiaryImage().getImageUrl() : null;
    this.likeCount = likeCount; // [수정] diary 객체 대신 파라미터로 받은 값을 사용
    this.commentCount = commentCount; // 주석 해제 및 파라미터 값 사용
    this.createdAt = diary.getCreatedAt();
  }
}
