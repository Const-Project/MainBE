package com.example.cp_main_be.domain.social.like.avatar_post.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.like.avatar_post.domain.AvatarPostLike;
import com.example.cp_main_be.domain.social.like.avatar_post.repository.AvatarPostLikeRepository;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.event.LikeCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AvatarPostLikeService {

  private final AvatarPostLikeRepository avatarPostLikeRepository;
  private final UserService userService;
  private final AvatarPostRepository avatarPostRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void likeAvatarPost(Long userId, Long postId) {
    User user = userService.findById(userId);
    AvatarPost post =
        avatarPostRepository
            .findById(postId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.POST_NOT_FOUND));

    if (avatarPostLikeRepository.existsByUserAndAvatarPost(user, post)) {
      throw new CustomApiException(ErrorCode.LIKE_ALREADY_EXISTS);
    }

    AvatarPostLike avatarPostLike = AvatarPostLike.builder().user(user).avatarPost(post).build();
    avatarPostLikeRepository.save(avatarPostLike);

    eventPublisher.publishEvent(
        new LikeCreatedEvent(post.getUser(), user, post.getId(), "AVATAR_POST"));
  }

  @Transactional
  public void unlikeAvatarPost(Long userId, Long postId) {
    User user = userService.findById(userId);
    AvatarPost post =
        avatarPostRepository
            .findById(postId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.POST_NOT_FOUND));

    if (!avatarPostLikeRepository.existsByUserAndAvatarPost(user, post)) {
      throw new CustomApiException(ErrorCode.LIKE_NOT_FOUND);
    }

    avatarPostLikeRepository.deleteByUserAndAvatarPost(user, post);
  }
}
