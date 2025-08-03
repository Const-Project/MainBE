package com.example.cp_main_be.domain.social.like.service;

import com.example.cp_main_be.domain.notification.domain.NotificationType;
import com.example.cp_main_be.domain.notification.service.NotificationService;
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
  private final NotificationService notificationService;

  public void addLike(Long userId, Long targetId, String targetType) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

    if (likeRepository.existsByUserAndTargetIdAndTargetType(user, targetId, targetType)) {
      throw new RuntimeException("이미 좋아요를 눌렀습니다."); // TODO: Custom Exception
    }

    Like like = Like.builder().user(user).targetId(targetId).targetType(targetType).build();
    likeRepository.save(like);

    if ("feed".equalsIgnoreCase(targetType)) {
      Feed feed =
          feedRepository
              .findById(targetId)
              .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));
      User receiver = feed.getUser();

      // 자기 자신에게는 알림을 보내지 않음
      if (!receiver.getId().equals(userId)) {
        notificationService.send(receiver, user, NotificationType.FEED_LIKE, "/feeds/" + targetId);
      }
    }
  }

  public void removeLike(Long userId, Long targetId, String targetType) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

    Like like =
        likeRepository
            .findByUserAndTargetIdAndTargetType(user, targetId, targetType)
            .orElseThrow(() -> new RuntimeException("좋아요를 찾을 수 없습니다.")); // TODO: Custom Exception
    likeRepository.delete(like);
  }
}
