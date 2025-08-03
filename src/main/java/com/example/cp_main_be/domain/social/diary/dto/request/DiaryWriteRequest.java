package com.example.cp_main_be.domain.social.diary.dto.request;

import lombok.Getter;

@Getter
public class DiaryWriteRequest {

  private String title;
  private String content;
  private String imageUrl;
  private String keyword;
}
