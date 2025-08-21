package com.example.cp_main_be.domain.social.like.service;

import com.example.cp_main_be.domain.member.notification.domain.NotificationType;
import com.example.cp_main_be.domain.member.notification.service.NotificationService;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.social.feed.domain.Feed;
import com.example.cp_main_be.domain.social.feed.domain.repository.FeedRepository;
import com.example.cp_main_be.domain.social.like.domain.Like;
import com.example.cp_main_be.domain.social.like.domain.repository.LikeRepository;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

  private final LikeRepository likeRepository;
  private final UserRepository userRepository;
  private final FeedRepository feedRepository;
  private final DiaryRepository diaryRepository;
  private final AvatarPostRepository avatarPostRepository;
  private final NotificationService notificationService;

  public int addLike(Long userId, Long targetId, String targetType) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

    if (likeRepository.existsByUserAndTargetIdAndTargetType(user, targetId, targetType)) {
      throw new RuntimeException("이미 좋아요를 눌렀습니다."); // TODO: Custom Exception
    }

    Like like = Like.builder().user(user).targetId(targetId).targetType(targetType).build();
    likeRepository.save(like);

    // 좋아요 알림 로직
    if ("feed".equalsIgnoreCase(targetType)) {
      Feed feed =
          feedRepository
              .findById(targetId)
              .orElseThrow(() -> new IllegalArgumentException("피드를 찾을 수 없습니다."));
      User receiver = feed.getUser();

      // 자기 자신에게는 알림을 보내지 않음
      if (!receiver.getId().equals(userId)) {
        notificationService.send(receiver, user, NotificationType.FEED_LIKE, "/feeds/" + targetId);
      }

      // 일단 보류
      return 0;
    } else if ("DIARY".equalsIgnoreCase(targetType)) {
      Diary diary =
          diaryRepository
              .findById(targetId)
              .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));
      diary.increaseLikeCount();

      User receiver = diary.getUser();
      if (!receiver.getId().equals(userId)) {
        notificationService.send(
            receiver, user, NotificationType.DIARY_LIKE, "/diaries/" + targetId);
      }
      return diary.getLikeCount();
    } else if ("AVATAR_POST".equalsIgnoreCase(targetType)) {
      AvatarPost avatarPost =
          avatarPostRepository
              .findById(targetId)
              .orElseThrow(() -> new IllegalArgumentException("포스트를 찾을 수 없습니다."));
      avatarPost.increaseLikeCount();

      User receiver = avatarPost.getUser();
      if (!receiver.getId().equals(userId)) {
        notificationService.send(
            receiver, user, NotificationType.AVATAR_POST_LIKE, "/avatar-posts/" + targetId);
      }
      return avatarPost.getLikeCount();
    }
    return 0;
  }

  public int removeLike(Long userId, Long targetId, String targetType) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

    Like like =
        likeRepository
            .findByUserAndTargetIdAndTargetType(user, targetId, targetType)
            .orElseThrow(() -> new RuntimeException("좋아요를 찾을 수 없습니다.")); // TODO: Custom Exception
    likeRepository.delete(like);

    if ("diary".equalsIgnoreCase(targetType)) {
      Diary diary =
          diaryRepository
              .findById(targetId)
              .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

      diary.decreaseLikeCount();
      return diary.getLikeCount();
    } else if ("avatar_post".equalsIgnoreCase(targetType)) {
      AvatarPost avatarPost =
          avatarPostRepository
              .findById(targetId)
              .orElseThrow(() -> new IllegalArgumentException("포스트를 찾을 수 없습니다."));

      avatarPost.decreaseLikeCount();
      return avatarPost.getLikeCount();
    }
    return 0;
  }
}
