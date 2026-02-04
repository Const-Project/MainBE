package com.example.cp_main_be.domain.social.feed.dto.request;

import lombok.Getter;

@Getter
public class RandomFeedSessionNextRequest {
  private String sessionToken;
  private int size = 20;
}
