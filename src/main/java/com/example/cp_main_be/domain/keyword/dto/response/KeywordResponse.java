package com.example.cp_main_be.domain.keyword.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class KeywordResponse {

  private final LocalDateTime date;
  private final String keyword;
}
