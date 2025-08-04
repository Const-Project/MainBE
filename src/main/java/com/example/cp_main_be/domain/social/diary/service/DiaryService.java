package com.example.cp_main_be.domain.social.diary.service;

import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.social.diary.dto.request.DiaryWriteRequest;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryIdResponse;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryResponse;
import com.example.cp_main_be.domain.user.domain.User;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryService {

  private final DiaryRepository diaryRepository;

  public Diary getDiaryById(Long diaryId) {
    return diaryRepository.findById(diaryId).orElseThrow();
  }

  public List<DiaryResponse> findAllDiariesByUserId(Long userId) {
    List<Diary> diaries = diaryRepository.findAllByUser_id(userId);
    return diaries.stream().map(DiaryResponse::from).collect(Collectors.toList());
  }

  public Long registerDiary(
      String title, String content, String imageUrl, String keyword, User user, Boolean isPublic) {
    Diary diary =
        Diary.builder()
            .title(title)
            .content(content)
            .imageUrl(imageUrl)
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
    Optional<Diary> optionalDiary = diaryRepository.findById(diaryId);
    if (optionalDiary.isEmpty()) {
      return null;
    }
    Diary diary = optionalDiary.get();
    diary.updateDiary(
        request.getTitle(), request.getContent(), request.getImageUrl(), request.isPublic());

    // 수정된 다이어리 반환
    return diaryRepository.save(diary);
  }
}
