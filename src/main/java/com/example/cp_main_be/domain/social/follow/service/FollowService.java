package com.example.cp_main_be.domain.social.follow.service;

import com.example.cp_main_be.domain.member.notification.domain.NotificationType;
import com.example.cp_main_be.domain.member.notification.service.NotificationService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.social.follow.domain.Follow;
import com.example.cp_main_be.domain.social.follow.domain.repository.FollowRepository;
import com.example.cp_main_be.domain.social.follow.dto.FollowResponseDTO;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {

  private final FollowRepository followRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService; // 알림 서비스 주입

  public void followUser(Long followerId, Long followingId) {
    User follower =
        userRepository
            .findById(followerId)
            .orElseThrow(() -> new UserNotFoundException("팔로워 사용자를 찾을 수 없습니다."));
    User following =
        userRepository
            .findById(followingId)
            .orElseThrow(() -> new UserNotFoundException("팔로잉할 사용자를 찾을 수 없습니다."));

    if (follower.getId().equals(following.getId()))
      throw new CustomApiException(ErrorCode.SELF_FOLLOWING_UNABLE);
    if (followRepository.existsByFollowerAndFollowing(follower, following)) {
      throw new RuntimeException("이미 팔로우한 사용자입니다."); // TODO: Custom Exception
    }

    Follow follow = Follow.builder().follower(follower).following(following).build();
    followRepository.save(follow);

    // 알림 전송
    notificationService.send(
        following, follower, NotificationType.FOLLOW, "/users/" + follower.getId(), null);
  }

  public void unfollowUser(Long followerId, Long followingId) {
    User follower =
        userRepository
            .findById(followerId)
            .orElseThrow(() -> new UserNotFoundException("팔로워 사용자를 찾을 수 없습니다."));
    User following =
        userRepository
            .findById(followingId)
            .orElseThrow(() -> new UserNotFoundException("언팔로우할 사용자를 찾을 수 없습니다."));

    Follow follow =
        followRepository
            .findByFollowerAndFollowing(follower, following)
            .orElseThrow(
                () -> new RuntimeException("팔로우 관계를 찾을 수 없습니다.")); // TODO: Custom Exception
    followRepository.delete(follow);
  }

  @Transactional(readOnly = true)
  public List<FollowResponseDTO> getFollowers(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

    List<User> userList =
        followRepository.findByFollowing(user).stream().map(Follow::getFollower).toList();

    return userList.stream()
        .map(
            member ->
                FollowResponseDTO.builder()
                    .username(member.getNickname())
                    .userImageUrl(
                        member.getAvatarList().isEmpty()
                            ? null
                            : member.getAvatarList().get(0).getImageUrl())
                    .userId(member.getId())
                    .build())
        .toList();
  }

  @Transactional(readOnly = true)
  public List<FollowResponseDTO> getFollowing(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
    List<User> userList =
        followRepository.findByFollower(user).stream().map(Follow::getFollowing).toList();

    return userList.stream()
        .map(
            member ->
                FollowResponseDTO.builder()
                    .username(member.getNickname())
                    .userImageUrl(
                        member.getAvatarList().isEmpty()
                            ? null
                            : member.getAvatarList().get(0).getImageUrl())
                    .userId(member.getId())
                    .build())
        .toList();
  }
}
