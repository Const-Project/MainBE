package com.example.cp_main_be.domain.mission.diaryimage.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.mission.diaryimage.domain.DiaryImage;
import com.example.cp_main_be.domain.mission.diaryimage.domain.DiaryImageRepository;
import com.example.cp_main_be.domain.mission.diaryimage.dto.response.ImageUploadResponse;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.infra.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DiaryImageService {

  private final DiaryImageRepository diaryImageRepository;
  private final DiaryRepository diaryRepository;
  private final S3Uploader s3Uploader;

  /** 이미지를 S3에 업로드하고, DiaryImage 엔티티를 생성하여 DB에 저장합니다. 아직 Diary와는 연결되지 않은 상태입니다. */
  public ImageUploadResponse uploadDiaryImage(MultipartFile file, User user) {
    String imageUrl = s3Uploader.upload(file, "diary-images");

    DiaryImage diaryImage =
        DiaryImage.builder()
            .imageUrl(imageUrl)
            .user(user) // 이미지 업로더를 기록하여 추후 권한 검증에 사용
            .build();

    DiaryImage savedImage = diaryImageRepository.save(diaryImage);

    return ImageUploadResponse.from(savedImage);
  }

  public void deleteDiaryImage(Long diaryId, Long imageId, Long userId) {
    DiaryImage image =
        diaryImageRepository
            .findById(imageId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.IMAGE_NOT_FOUND));

    // Diary와 연결되어 있다면 diaryId 일치 여부 확인
    if (image.getDiary() != null) {
      if (!image.getDiary().getId().equals(diaryId)) {
        throw new CustomApiException(ErrorCode.INVALID_REQUEST, "요청한 일기와 이미지가 일치하지 않습니다.");
      }
      // 작성자 권한 검증
      if (!image.getDiary().getUser().getId().equals(userId)) {
        throw new CustomApiException(ErrorCode.ACCESS_DENIED);
      }
      image.getDiary().setDiaryImage(null);
    } else {
      // Diary에 미연결된 임시 이미지의 경우 업로더만 삭제 가능
      if (!image.getUser().getId().equals(userId)) {
        throw new CustomApiException(ErrorCode.ACCESS_DENIED);
      }
    }

    // S3 객체 삭제 (메서드 존재 시)
    try {
      s3Uploader.delete(image.getImageUrl());
    } catch (Exception ignored) {
      // S3에서 객체 삭제에 실패하더라도 DB에서는 삭제를 계속 진행합니다.
      log.warn("S3 이미지 삭제 실패. DB에서는 삭제를 계속합니다. URL: {}", image.getImageUrl(), ignored);
    }
    diaryImageRepository.delete(image);
  }
}
