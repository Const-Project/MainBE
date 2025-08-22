package com.example.cp_main_be.domain.social.follow.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.cp_main_be.domain.member.notification.service.NotificationService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.social.follow.domain.Follow;
import com.example.cp_main_be.domain.social.follow.domain.repository.FollowRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

  @Mock private FollowRepository followRepository;
  @Mock private UserRepository userRepository;

  @InjectMocks private FollowService followService;
  @Mock private NotificationService notificationService;

  @DisplayName("팔로우 성공")
  @Test
  void followUser_success() {
    // given
    Long followerId = 1L;
    Long followingId = 2L;
    User follower = User.builder().id(followerId).nickname("follower").build();
    User following = User.builder().id(followingId).nickname("following").build();

    given(userRepository.findById(followerId)).willReturn(Optional.of(follower));
    given(userRepository.findById(followingId)).willReturn(Optional.of(following));
    given(followRepository.existsByFollowerAndFollowing(follower, following)).willReturn(false);

    // when
    followService.followUser(followerId, followingId);

    // then
    verify(followRepository).save(any(Follow.class));
  }

  @DisplayName("팔로우 실패 - 이미 팔로우한 사용자")
  @Test
  void followUser_fail_alreadyFollowing() {
    // given
    Long followerId = 1L;
    Long followingId = 2L;
    User follower = User.builder().id(followerId).nickname("follower").build();
    User following = User.builder().id(followingId).nickname("following").build();

    given(userRepository.findById(followerId)).willReturn(Optional.of(follower));
    given(userRepository.findById(followingId)).willReturn(Optional.of(following));
    given(followRepository.existsByFollowerAndFollowing(follower, following)).willReturn(true);

    // when & then
    Assertions.assertThrows(
        RuntimeException.class, () -> followService.followUser(followerId, followingId));
    verify(followRepository, org.mockito.Mockito.never()).save(any(Follow.class));
  }

  @DisplayName("언팔로우 성공")
  @Test
  void unfollowUser_success() {
    // given
    Long followerId = 1L;
    Long followingId = 2L;
    User follower = User.builder().id(followerId).nickname("follower").build();
    User following = User.builder().id(followingId).nickname("following").build();
    Follow follow = Follow.builder().follower(follower).following(following).build();

    given(userRepository.findById(followerId)).willReturn(Optional.of(follower));
    given(userRepository.findById(followingId)).willReturn(Optional.of(following));
    given(followRepository.findByFollowerAndFollowing(follower, following))
        .willReturn(Optional.of(follow));

    // when
    followService.unfollowUser(followerId, followingId);

    // then
    verify(followRepository).delete(follow);
  }

  @DisplayName("언팔로우 실패 - 팔로우 관계를 찾을 수 없음")
  @Test
  void unfollowUser_fail_followNotFound() {
    // given
    Long followerId = 1L;
    Long followingId = 2L;
    User follower = User.builder().id(followerId).nickname("follower").build();
    User following = User.builder().id(followingId).nickname("following").build();

    given(userRepository.findById(followerId)).willReturn(Optional.of(follower));
    given(userRepository.findById(followingId)).willReturn(Optional.of(following));
    given(followRepository.findByFollowerAndFollowing(follower, following))
        .willReturn(Optional.empty());

    // when & then
    Assertions.assertThrows(
        RuntimeException.class, () -> followService.unfollowUser(followerId, followingId));
    verify(followRepository, org.mockito.Mockito.never()).delete(any(Follow.class));
  }

  @DisplayName("팔로워 목록 조회 성공")
  @Test
  void getFollowers_success() {
    // given
    Long userId = 1L;
    User user = User.builder().id(userId).nickname("user").build();
    User follower1 = User.builder().id(2L).nickname("follower1").build();
    User follower2 = User.builder().id(3L).nickname("follower2").build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(followRepository.findByFollowing(user))
        .willReturn(
            Arrays.asList(
                Follow.builder().follower(follower1).following(user).build(),
                Follow.builder().follower(follower2).following(user).build()));

    // when
    List<User> followers = followService.getFollowers(userId);

    // then
    Assertions.assertEquals(2, followers.size());
    Assertions.assertTrue(followers.contains(follower1));
    Assertions.assertTrue(followers.contains(follower2));
    verify(userRepository).findById(userId);
    verify(followRepository).findByFollowing(user);
  }

  @DisplayName("팔로잉 목록 조회 성공")
  @Test
  void getFollowing_success() {
    // given
    Long userId = 1L;
    User user = User.builder().id(userId).nickname("user").build();
    User following1 = User.builder().id(2L).nickname("following1").build();
    User following2 = User.builder().id(3L).nickname("following2").build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(followRepository.findByFollower(user))
        .willReturn(
            Arrays.asList(
                Follow.builder().follower(user).following(following1).build(),
                Follow.builder().follower(user).following(following2).build()));

    // when
    List<User> following = followService.getFollowing(userId);

    // then
    Assertions.assertEquals(2, following.size());
    Assertions.assertTrue(following.contains(following1));
    Assertions.assertTrue(following.contains(following2));
    verify(userRepository).findById(userId);
    verify(followRepository).findByFollower(user);
  }
}
