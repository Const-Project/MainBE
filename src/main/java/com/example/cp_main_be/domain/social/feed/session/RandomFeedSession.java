package com.example.cp_main_be.domain.social.feed.session;

import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RandomFeedSession {
  private List<Long> diaryIds;
  private List<Long> avatarPostIds;
  private int diaryCursor;
  private int avatarPostCursor;
  private Instant expiresAt;

  public RandomFeedSession(
      List<Long> diaryIds,
      List<Long> avatarPostIds,
      int diaryCursor,
      int avatarPostCursor,
      Instant expiresAt) {
    this.diaryIds = diaryIds;
    this.avatarPostIds = avatarPostIds;
    this.diaryCursor = diaryCursor;
    this.avatarPostCursor = avatarPostCursor;
    this.expiresAt = expiresAt;
  }

  public void advanceDiaryCursor(int delta) {
    this.diaryCursor += delta;
  }

  public void advanceAvatarPostCursor(int delta) {
    this.avatarPostCursor += delta;
  }
}
