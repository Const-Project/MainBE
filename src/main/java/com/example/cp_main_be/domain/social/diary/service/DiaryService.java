package com.example.cp_main_be.domain.social.diary.service;

import com.example.cp_main_be.domain.content.image.ImageUploader;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.social.diary.dto.request.CreateDiaryRequest;
import com.example.cp_main_be.domain.social.diary.dto.request.UpdateDiaryRequest;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryResponse;
import com.example.cp_main_be.domain.social.diaryimage.domain.DiaryImage;
import com.example.cp_main_be.domain.social.diaryimage.domain.DiaryImageRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryService {

  private final DiaryRepository diaryRepository;
  private final UserRepository userRepository;
  private final ImageUploader imageUploader; // 의존성 주입은 인터페이스로
  private final DiaryImageRepository diaryImageRepository;

  public Diary createDiary(User user, CreateDiaryRequest request) {
    // 1. 먼저 Diary 객체를 생성하고 저장합니다
    Diary diary =
        Diary.builder()
            .user(user)
            .title(request.getTitle())
            .content(request.getContent())
            .isPublic(request.getIsPublic())
            .build();

    // 2. Diary를 먼저 저장하여 ID를 생성합니다
    Diary savedDiary = diaryRepository.save(diary);

    // 3. 이미지가 있다면 DiaryImage를 생성하고 연관관계를 설정합니다
    if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
      DiaryImage diaryImage =
          DiaryImage.builder()
              .imageUrl(request.getImageUrl())
              .diary(savedDiary) // ✅ 연관관계의 주인인 DiaryImage에 Diary를 설정
              .build();

      diaryImageRepository.save(diaryImage);
    }

    return savedDiary;
  }

  // 일기 상세 조회 (읽기 전용)
  @Transactional(readOnly = true)
  public Diary findDiaryById(Long diaryId) {
    return diaryRepository
        .findById(diaryId)
        .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));
  }

  // 내 일기 목록 조회 (읽기 전용)
  @Transactional(readOnly = true)
  public List<Diary> findMyDiaries(User user) {
    return diaryRepository.findByUserOrderByCreatedAtDesc(user);
  }

  public Diary updateDiary(Long userId, Long diaryId, UpdateDiaryRequest request) {
    Diary diary = findDiaryById(diaryId);

    if (!Objects.equals(diary.getUser().getId(), userId)) {
      throw new SecurityException("일기를 수정할 권한이 없습니다.");
    }

    // 일기 내용 수정
    diary.updateDiary(request.getTitle(), request.getContent(), request.getIsPublic());

    // 이미지 처리
    DiaryImage existingImage = diary.getDiaryImage();

    if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
      if (existingImage != null) {
        // 기존 이미지가 있으면 URL만 업데이트
        existingImage.updateImageUrl(request.getImageUrl());
      } else {
        // 기존 이미지가 없으면 새로 생성
        DiaryImage newDiaryImage =
            DiaryImage.builder().imageUrl(request.getImageUrl()).diary(diary).build();
        diaryImageRepository.save(newDiaryImage);
      }
    } else {
      // 이미지 URL이 null이거나 빈 문자열이면 기존 이미지 삭제
      if (existingImage != null) {
        diaryImageRepository.delete(existingImage);
      }
    }

    return diary;
  }

  // 일기 삭제
  public void deleteDiary(Long userId, Long diaryId) {
    Diary diary = findDiaryById(diaryId);

    // 소유권 확인
    if (!Objects.equals(diary.getUser().getId(), userId)) {
      throw new SecurityException("일기를 삭제할 권한이 없습니다.");
    }

    diaryRepository.delete(diary);
  }

  public DiaryResponse saveDiaryImage(Long diaryId, MultipartFile file) {
    // 1. 현재 로그인한 사용자 정보 가져오기
    String uuidString =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    UUID userUuid = UUID.fromString(uuidString);
    User user =
        userRepository
            .findByUuid(userUuid)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

    // 2. 다이어리 조회
    Diary diary =
        diaryRepository
            .findById(diaryId)
            .orElseThrow(() -> new IllegalArgumentException("해당 다이어리가 존재하지 않습니다."));

    // 3. 권한 검증: 일기 작성자와 현재 사용자가 동일한지 확인
    if (!diary.getUser().getId().equals(user.getId())) {
      throw new IllegalStateException("해당 다이어리에 이미지를 추가할 권한이 없습니다.");
    }

    // 4. 이미지 파일을 업로더에 전달하고 URL 받기
    String imageUrl = imageUploader.upload(file, "diary-images");

    DiaryImage diaryImage = DiaryImage.builder().imageUrl(imageUrl).diary(diary).build();

    // 새로 생성된 DiaryImage를 명시적으로 저장합니다.
    diaryImageRepository.save(diaryImage);

    // 5. 다이어리 엔티티의 imageUrl 필드 업데이트
    diary.updateImage(diaryImage);

    return DiaryResponse.from(diary);
  }

  public void deleteDiaryImage(Long diaryId, Long imageId) {
    // 1. 현재 로그인한 사용자 정보 가져오기
    String uuidString =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    UUID userUuid = UUID.fromString(uuidString);
    User user =
        userRepository
            .findByUuid(userUuid)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

    // 2. 다이어리 조회 및 권한 검증
    Diary diary =
        diaryRepository
            .findById(diaryId)
            .orElseThrow(() -> new IllegalArgumentException("해당 다이어리가 존재하지 않습니다."));
    if (!diary.getUser().getId().equals(user.getId())) {
      throw new IllegalStateException("해당 다이어리에 이미지를 삭제할 권한이 없습니다.");
    }

    // 3. 이미지 엔티티 조회 및 소유권 검증
    DiaryImage diaryImage =
        diaryImageRepository
            .findById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("해당 이미지가 존재하지 않습니다."));
    if (!diaryImage.getDiary().getId().equals(diaryId)) {
      throw new IllegalArgumentException("해당 이미지는 다이어리에 속하지 않습니다.");
    }

    // 4. 클라우드 스토리지(S3)에서 실제 파일 삭제
    imageUploader.delete(diaryImage.getImageUrl());

    diaryImageRepository.deleteById(diaryImage.getId());
  }
}
