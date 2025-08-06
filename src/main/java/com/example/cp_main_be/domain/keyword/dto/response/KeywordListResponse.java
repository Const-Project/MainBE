package com.example.cp_main_be.domain.keyword.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class KeywordListResponse {

  private final List<String> keywords;
}
