package com.example.cp_main_be.domain.social.diary.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.Repository.DiaryRepository;
import com.example.cp_main_be.domain.social.diary.dto.request.CreateDiaryRequest;
import com.example.cp_main_be.domain.social.diary.dto.request.UpdateDiaryRequest;
import com.example.cp_main_be.domain.social.diaryimage.domain.DiaryImage;
import com.example.cp_main_be.domain.social.diaryimage.domain.DiaryImageRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryService {

  private final DiaryRepository diaryRepository;
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
}
