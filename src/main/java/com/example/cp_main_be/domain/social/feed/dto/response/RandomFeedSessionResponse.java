package com.example.cp_main_be.domain.social.feed.dto.response;

import com.example.cp_main_be.global.dto.FeedItemResponse;
import java.util.List;
import lombok.Getter;

@Getter
public class RandomFeedSessionResponse {
  private final String sessionToken;
  private final List<FeedItemResponse> items;
  private final boolean hasMore;

  public RandomFeedSessionResponse(
      String sessionToken, List<FeedItemResponse> items, boolean hasMore) {
    this.sessionToken = sessionToken;
    this.items = items;
    this.hasMore = hasMore;
  }
}
