package com.example.cp_main_be.domain.social.like.diary.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.domain.member.userblock.UserBlockRepository;
import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import com.example.cp_main_be.domain.mission.diary.service.DiaryService;
import com.example.cp_main_be.domain.social.like.diary.domain.DiaryLike;
import com.example.cp_main_be.domain.social.like.diary.repository.DiaryLikeRepository;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.event.LikeCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiaryLikeService {

  private final DiaryLikeRepository diaryLikeRepository;
  private final UserService userService;
  private final DiaryService diaryService;
  private final UserBlockRepository userBlockRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void likeDiary(Long userId, Long diaryId) {
    User user = userService.findById(userId);
    Diary diary = diaryService.findDiaryById(diaryId);

    if (userBlockRepository.existsByBlockerUserAndBlockedUser(diary.getUser(), user)
        || userBlockRepository.existsByBlockerUserAndBlockedUser(user, diary.getUser())) {
      throw new CustomApiException(ErrorCode.ACCESS_DENIED, "차단 상태에서는 좋아요를 할 수 없습니다.");
    }

    if (diaryLikeRepository.existsByUserAndDiary(user, diary)) {
      throw new CustomApiException(ErrorCode.LIKE_ALREADY_EXISTS);
    }

    DiaryLike diaryLike = DiaryLike.builder().user(user).diary(diary).build();
    diaryLikeRepository.save(diaryLike);

    eventPublisher.publishEvent(
        new LikeCreatedEvent(diary.getUser(), user, diary.getId(), "DIARY"));
  }

  @Transactional
  public void unlikeDiary(Long userId, Long diaryId) {
    User user = userService.findById(userId);
    Diary diary = diaryService.findDiaryById(diaryId);

    if (userBlockRepository.existsByBlockerUserAndBlockedUser(diary.getUser(), user)
        || userBlockRepository.existsByBlockerUserAndBlockedUser(user, diary.getUser())) {
      throw new CustomApiException(ErrorCode.ACCESS_DENIED, "차단 상태에서는 좋아요를 취소할 수 없습니다.");
    }

    if (!diaryLikeRepository.existsByUserAndDiary(user, diary)) {
      throw new CustomApiException(ErrorCode.LIKE_NOT_FOUND);
    }

    diaryLikeRepository.deleteByUserAndDiary(user, diary);
  }
}
