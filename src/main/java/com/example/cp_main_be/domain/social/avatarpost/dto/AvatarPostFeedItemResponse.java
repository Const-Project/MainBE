package com.example.cp_main_be.domain.social.avatarpost.dto;

import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.global.dto.AuthorResponse;
import com.example.cp_main_be.global.dto.FeedItemResponse;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class AvatarPostFeedItemResponse implements FeedItemResponse {
  private Long postId;
  private final String postType = "AVATAR_POST";
  private AuthorResponse author;
  private String caption;
  private int likeCount;
  private int commentCount;
  private LocalDateTime createdAt;

  public AvatarPostFeedItemResponse(AvatarPost post) {
    this.postId = post.getId();
    this.author = new AuthorResponse(post.getUser());
    this.caption = post.getCaption();
    this.likeCount = post.getLikeCount();
    this.commentCount = post.getComments() != null ? post.getComments().size() : 0;
    this.createdAt = post.getCreatedAt();
  }
}
