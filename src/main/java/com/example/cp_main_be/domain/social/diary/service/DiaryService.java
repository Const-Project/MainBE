package com.example.cp_main_be.domain.social.diary.service;

import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.social.diary.dto.request.DiaryWriteRequest;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryIdResponse;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryResponse;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryService {

  private final DiaryRepository diaryRepository;
  private final UserRepository userRepository;

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
    diary.updateDiary(
        request.getTitle(), request.getContent(), request.getImageUrl(), request.isPublic());

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
}
