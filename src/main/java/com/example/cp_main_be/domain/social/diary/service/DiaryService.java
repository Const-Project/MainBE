package com.example.cp_main_be.domain.social.diary.service;

import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryIdResponse;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryResponse;
import com.example.cp_main_be.domain.user.domain.User;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryService {

  private final DiaryRepository diaryRepository;

  public List<DiaryResponse> findAllDiariesByUserId(Long userId) {
    List<Diary> diaries = diaryRepository.findAllByUser_id(userId);
    return diaries.stream().map(DiaryResponse::from).collect(Collectors.toList());
  }

  public Long registerDiary(
      String title, String content, String imageUrl, String keyword, User user) {
    Diary diary =
        Diary.builder()
            .title(title)
            .content(content)
            .imageUrl(imageUrl)
            .keyword(keyword)
            .user(user)
            .build();
    diaryRepository.save(diary);
    return diary.getId();
  }

  public DiaryIdResponse findDiaryById(Long diaryId) {
    Diary diary = diaryRepository.findDiaryById(diaryId);
    return DiaryIdResponse.builder().id(diary.getId()).build();
  }
}
