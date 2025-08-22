package com.example.cp_main_be.domain.social.avatarpost.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostLikeResponse {
  private int likeCount;
}
