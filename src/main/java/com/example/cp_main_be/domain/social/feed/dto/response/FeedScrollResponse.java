package com.example.cp_main_be.domain.social.feed.dto.response;

import com.example.cp_main_be.global.dto.FeedItemResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FeedScrollResponse {
  private List<FeedItemResponse> items;
  private boolean hasMore;
}
