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
  private final int likeCount;
  private int commentCount; // Diary에 Comment 리스트가 있다고 가정
  private final LocalDateTime createdAt;

  public DiaryFeedItemResponse(Diary diary) {
    this.postId = diary.getId();
    this.author = new AuthorResponse(diary.getUser());
    this.title = diary.getTitle();
    this.content = diary.getContent();
    this.imageUrl = diary.getDiaryImage() != null ? diary.getDiaryImage().getImageUrl() : null;
    this.likeCount = diary.getLikeCount();
    this.createdAt = diary.getCreatedAt();
    // this.commentCount = diary.getComments().size(); // Diary에 OneToMany Comment 관계가 필요
  }
}
