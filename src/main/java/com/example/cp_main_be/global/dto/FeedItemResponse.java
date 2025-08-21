package com.example.cp_main_be.global.dto;

import java.time.LocalDateTime;

public interface FeedItemResponse {
  Long getPostId();

  String getPostType(); // "DIARY" 또는 "AVATAR_POST"

  AuthorResponse getAuthor();

  int getLikeCount();

  int getCommentCount();

  LocalDateTime getCreatedAt();
}
