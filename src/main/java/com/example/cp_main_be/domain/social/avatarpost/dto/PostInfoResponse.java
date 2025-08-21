package com.example.cp_main_be.domain.social.avatarpost.dto;

import com.example.cp_main_be.domain.social.comment.domain.Comment;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PostInfoResponse {

  private String imageUrl;
  private int likeCount;
  private List<Comment> comments;
  private boolean isBookmarked;
}
