package com.example.cp_main_be.domain.mission.diaryimage.dto.response;

import com.example.cp_main_be.domain.mission.diaryimage.domain.DiaryImage;

/** 이미지 업로드 후 결과(ID, URL)를 반환하는 DTO입니다. */
public record ImageUploadResponse(Long imageId, String imageUrl) {

  public static ImageUploadResponse from(DiaryImage diaryImage) {
    return new ImageUploadResponse(diaryImage.getId(), diaryImage.getImageUrl());
  }
}
