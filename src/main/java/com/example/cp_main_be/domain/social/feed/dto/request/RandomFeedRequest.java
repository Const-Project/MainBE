package com.example.cp_main_be.domain.social.feed.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RandomFeedRequest {
  private List<Long> excludeDiaryIds;
  private List<Long> excludeAvatarPostIds;

  @Min(1)
  @Max(50)
  private int size = 10;
}
