package com.example.cp_main_be.domain.social.like.service;

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
