package com.example.cp_main_be.domain.social.follow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import com.example.cp_main_be.domain.member.notification.service.NotificationService;
import com.example.cp_main_be.domain.member.user.domain.Role;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.userblock.UserBlockRepository;
import com.example.cp_main_be.domain.social.follow.domain.Follow;
import com.example.cp_main_be.domain.social.follow.domain.repository.FollowRepository;
import com.example.cp_main_be.domain.social.follow.dto.FollowResponseDTO;
import com.example.cp_main_be.global.common.CustomApiException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

  @Mock private FollowRepository followRepository;
  @Mock private UserRepository userRepository;
  @Mock private NotificationService notificationService;
  @Mock private UserBlockRepository userBlockRepository;

  @InjectMocks private FollowService followService;

  private User createUser(Long id, String nickname) {
    return User.builder()
        .id(id)
        .uuid(UUID.randomUUID())
        .nickname(nickname)
        .role(Role.USER)
        .avatarList(new ArrayList<>())
        .build();
  }

  private User createUserWithAvatar(Long id, String nickname, String avatarImageUrl) {
    User user = createUser(id, nickname);
    Avatar avatar =
        Avatar.builder().id(1L).nickname("avatar").imageUrl(avatarImageUrl).user(user).build();
    user.getAvatarList().add(avatar);
    return user;
  }

  @Nested
  @DisplayName("getFollowers 메서드")
  class GetFollowersTest {

    @Test
    @DisplayName("팔로워 목록 조회 성공 - 아바타가 있는 경우")
    void getFollowers_success_withAvatar() {
      // given
      Long userId = 1L;
      User user = createUser(userId, "user");
      User follower1 = createUserWithAvatar(2L, "follower1", "http://image1.com");
      User follower2 = createUserWithAvatar(3L, "follower2", "http://image2.com");

      List<Follow> follows =
          Arrays.asList(
              Follow.builder().follower(follower1).following(user).build(),
              Follow.builder().follower(follower2).following(user).build());

      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(followRepository.findByFollowingWithFollowerAndAvatars(user)).willReturn(follows);

      // when
      List<FollowResponseDTO> result = followService.getFollowers(userId);

      // then
      assertThat(result).hasSize(2);
      assertThat(result.get(0).getUserId()).isEqualTo(2L);
      assertThat(result.get(0).getUsername()).isEqualTo("follower1");
      assertThat(result.get(0).getUserImageUrl()).isEqualTo("http://image1.com");
      assertThat(result.get(1).getUserId()).isEqualTo(3L);
      assertThat(result.get(1).getUsername()).isEqualTo("follower2");
      assertThat(result.get(1).getUserImageUrl()).isEqualTo("http://image2.com");

      verify(userRepository).findById(userId);
      verify(followRepository).findByFollowingWithFollowerAndAvatars(user);
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공 - 아바타가 없는 경우")
    void getFollowers_success_withoutAvatar() {
      // given
      Long userId = 1L;
      User user = createUser(userId, "user");
      User follower1 = createUser(2L, "follower1");

      List<Follow> follows = List.of(Follow.builder().follower(follower1).following(user).build());

      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(followRepository.findByFollowingWithFollowerAndAvatars(user)).willReturn(follows);

      // when
      List<FollowResponseDTO> result = followService.getFollowers(userId);

      // then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getUserId()).isEqualTo(2L);
      assertThat(result.get(0).getUsername()).isEqualTo("follower1");
      assertThat(result.get(0).getUserImageUrl()).isNull();
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공 - 팔로워가 없는 경우")
    void getFollowers_success_noFollowers() {
      // given
      Long userId = 1L;
      User user = createUser(userId, "user");

      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(followRepository.findByFollowingWithFollowerAndAvatars(user)).willReturn(List.of());

      // when
      List<FollowResponseDTO> result = followService.getFollowers(userId);

      // then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("팔로워 목록 조회 실패 - 사용자를 찾을 수 없음")
    void getFollowers_fail_userNotFound() {
      // given
      Long userId = 1L;
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> followService.getFollowers(userId))
          .isInstanceOf(CustomApiException.class);

      verify(followRepository, never()).findByFollowingWithFollowerAndAvatars(any());
    }
  }

  @Nested
  @DisplayName("getFollowing 메서드")
  class GetFollowingTest {

    @Test
    @DisplayName("팔로잉 목록 조회 성공 - 아바타가 있는 경우")
    void getFollowing_success_withAvatar() {
      // given
      Long userId = 1L;
      User user = createUser(userId, "user");
      User following1 = createUserWithAvatar(2L, "following1", "http://image1.com");
      User following2 = createUserWithAvatar(3L, "following2", "http://image2.com");

      List<Follow> follows =
          Arrays.asList(
              Follow.builder().follower(user).following(following1).build(),
              Follow.builder().follower(user).following(following2).build());

      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(followRepository.findByFollowerWithFollowingAndAvatars(user)).willReturn(follows);

      // when
      List<FollowResponseDTO> result = followService.getFollowing(userId);

      // then
      assertThat(result).hasSize(2);
      assertThat(result.get(0).getUserId()).isEqualTo(2L);
      assertThat(result.get(0).getUsername()).isEqualTo("following1");
      assertThat(result.get(0).getUserImageUrl()).isEqualTo("http://image1.com");
      assertThat(result.get(1).getUserId()).isEqualTo(3L);
      assertThat(result.get(1).getUsername()).isEqualTo("following2");
      assertThat(result.get(1).getUserImageUrl()).isEqualTo("http://image2.com");

      verify(userRepository).findById(userId);
      verify(followRepository).findByFollowerWithFollowingAndAvatars(user);
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공 - 아바타가 없는 경우")
    void getFollowing_success_withoutAvatar() {
      // given
      Long userId = 1L;
      User user = createUser(userId, "user");
      User following1 = createUser(2L, "following1");

      List<Follow> follows = List.of(Follow.builder().follower(user).following(following1).build());

      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(followRepository.findByFollowerWithFollowingAndAvatars(user)).willReturn(follows);

      // when
      List<FollowResponseDTO> result = followService.getFollowing(userId);

      // then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getUserId()).isEqualTo(2L);
      assertThat(result.get(0).getUsername()).isEqualTo("following1");
      assertThat(result.get(0).getUserImageUrl()).isNull();
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공 - 팔로잉이 없는 경우")
    void getFollowing_success_noFollowing() {
      // given
      Long userId = 1L;
      User user = createUser(userId, "user");

      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(followRepository.findByFollowerWithFollowingAndAvatars(user)).willReturn(List.of());

      // when
      List<FollowResponseDTO> result = followService.getFollowing(userId);

      // then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("팔로잉 목록 조회 실패 - 사용자를 찾을 수 없음")
    void getFollowing_fail_userNotFound() {
      // given
      Long userId = 1L;
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> followService.getFollowing(userId))
          .isInstanceOf(CustomApiException.class);

      verify(followRepository, never()).findByFollowerWithFollowingAndAvatars(any());
    }
  }
}
