package com.example.cp_main_be.domain.social.avatarpost.dto.response;

import com.example.cp_main_be.domain.social.comment.domain.Comment;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostInfoResponse {

  private String imageUrl;
  private int likeCount;
  private List<Comment> comments;
  private boolean isBookmarked;
}
