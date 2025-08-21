package com.example.cp_main_be.domain.social.diary.service;

import com.example.cp_main_be.domain.content.image.ImageUploader;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.social.diary.dto.request.DiaryWriteRequest;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryIdResponse;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryResponse;
import com.example.cp_main_be.domain.social.diaryimage.domain.DiaryImage;
import com.example.cp_main_be.domain.social.diaryimage.domain.DiaryImageRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryService {

  private final DiaryRepository diaryRepository;
  private final UserRepository userRepository;
  private final ImageUploader imageUploader; // 의존성 주입은 인터페이스로
  private final DiaryImageRepository diaryImageRepository;

  public Diary getDiaryById(Long diaryId) {
    return diaryRepository.findById(diaryId).orElseThrow();
  }

  public List<DiaryResponse> findAllDiariesByUserId(Long userId) {
    List<Diary> diaries = diaryRepository.findAllByUserId(userId);
    return diaries.stream().map(DiaryResponse::from).collect(Collectors.toList());
  }

  public Long registerDiary(
      String title, String content, String keyword, User user, Boolean isPublic) {
    Diary diary =
        Diary.builder()
            .title(title)
            .content(content)
            .keyword(keyword)
            .user(user)
            .isPublic(isPublic)
            .build();
    diaryRepository.save(diary);
    return diary.getId();
  }

  public DiaryIdResponse getDiaryIdResponseById(Long diaryId) {
    Diary diary = diaryRepository.findDiaryById(diaryId);
    return DiaryIdResponse.builder().id(diary.getId()).build();
  }

  public DiaryResponse getDiaryResponseById(Long diaryId) {
    // 아마 diaryId 로만 조회하면 남이 쓴 일기도 조회 가능할듯
    Diary diary = diaryRepository.findDiaryById(diaryId);
    return DiaryResponse.from(diary);
  }

  public Diary updateDiary(Long diaryId, @Valid DiaryWriteRequest request) {
    // 1. 현재 인증된 사용자의 UUID를 SecurityContext에서 가져옴
    String uuidString =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    UUID userUuid = UUID.fromString(uuidString);

    // 2. 다이어리와 사용자 조회 (없으면 예외 발생)
    Diary diary =
        diaryRepository
            .findById(diaryId)
            .orElseThrow(() -> new IllegalArgumentException("해당 다이어리가 존재하지 않습니다."));
    User user =
        userRepository
            .findByUuid(userUuid)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

    // 3. 권한 검증: 현재 사용자가 다이어리 소유자인지 확인
    if (!diary.getUser().getId().equals(user.getId())) {
      throw new IllegalStateException("해당 다이어리를 수정할 권한이 없습니다.");
    }

    // 4. 비즈니스 메서드를 사용해 다이어리 업데이트
    diary.updateDiary(request.getTitle(), request.getContent(), request.isPublic());

    return diary;
  }

  public void deleteDiaryById(Long diaryId) {
    // 1. 현재 로그인한 사용자 정보 가져오기
    String uuidString =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    UUID userUuid = UUID.fromString(uuidString);

    // 2. 다이어리와 사용자 조회 (findById, findByUuid 사용)
    Diary diary =
        diaryRepository
            .findById(diaryId)
            .orElseThrow(() -> new IllegalArgumentException("해당 다이어리가 존재하지 않습니다."));
    User user =
        userRepository
            .findByUuid(userUuid)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

    // 3. 권한 검증: 일기 작성자와 현재 사용자가 동일한지 확인
    if (!diary.getUser().getId().equals(user.getId())) {
      throw new IllegalStateException("해당 다이어리를 삭제할 권한이 없습니다.");
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
