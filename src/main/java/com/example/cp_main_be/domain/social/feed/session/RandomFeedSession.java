package com.example.cp_main_be.domain.social.feed.session;

import java.time.Instant;
import java.util.List;

public class RandomFeedSession {
  private final List<Long> diaryIds;
  private final List<Long> avatarPostIds;
  private int diaryCursor;
  private int avatarPostCursor;
  private final Instant expiresAt;

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

  public List<Long> getDiaryIds() {
    return diaryIds;
  }

  public List<Long> getAvatarPostIds() {
    return avatarPostIds;
  }

  public int getDiaryCursor() {
    return diaryCursor;
  }

  public int getAvatarPostCursor() {
    return avatarPostCursor;
  }

  public void advanceDiaryCursor(int delta) {
    this.diaryCursor += delta;
  }

  public void advanceAvatarPostCursor(int delta) {
    this.avatarPostCursor += delta;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }
}
