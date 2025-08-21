package com.example.cp_main_be.domain.mission.keyword.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class KeywordResponse {

  private final LocalDateTime date;
  private final String keyword;
}
