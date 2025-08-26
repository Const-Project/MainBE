package com.example.cp_main_be.domain.mission.diaryimage.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.mission.diaryimage.domain.DiaryImage;
import com.example.cp_main_be.domain.mission.diaryimage.domain.DiaryImageRepository;
import com.example.cp_main_be.domain.mission.diaryimage.dto.response.ImageUploadResponse;
import com.example.cp_main_be.global.infra.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
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

  // 기존 삭제 로직은 여기에 위치하는 것이 더 적합합니다.
  public void deleteDiaryImage(Long diaryId, Long imageId) {
    // TODO: 삭제 로직 구현 (작성자 또는 관리자만 삭제 가능하도록 권한 검증 필요)
  }
}
