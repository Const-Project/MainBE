package com.example.cp_main_be.domain.social.diary.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryWriteRequest {

  private String title;
  private String content;
  private String keyword;
  private boolean isPublic;
}
