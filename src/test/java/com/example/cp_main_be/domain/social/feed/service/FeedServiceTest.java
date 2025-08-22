package com.example.cp_main_be.domain.social.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.Repository.DiaryRepository;
import com.example.cp_main_be.domain.social.follow.domain.Follow;
import com.example.cp_main_be.domain.social.follow.domain.repository.FollowRepository;
import com.example.cp_main_be.global.dto.FeedItemResponse;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

  @InjectMocks private FeedService feedService;

  @Mock private UserRepository userRepository;

  @Mock private DiaryRepository diaryRepository;

  @Mock private FollowRepository followRepository;

  @Mock private AvatarPostRepository avatarPostRepository;

  @Test
  @DisplayName("피드 조회 성공 - 필터 없음, 게시물 시간순 정렬 확인")
  void getFeed_noFilter_returnsCombinedAndSortedFeed() {
    // given
    UUID userUuid = UUID.randomUUID();
    // 💡 1. Mock User 생성
    User mockUser = User.builder().uuid(UUID.randomUUID()).nickname("mockUser").build();

    // 💡 2. Mock 데이터 생성 시 user 설정
    Diary oldDiary =
        Diary.builder()
            .user(mockUser) // 사용자 설정
            .createdAt(LocalDateTime.now().minusDays(2))
            .build();
    AvatarPost recentAvatarPost =
        AvatarPost.builder()
            .user(mockUser) // 사용자 설정
            .createdAt(LocalDateTime.now().minusHours(1))
            .build();
    Diary recentDiary =
        Diary.builder()
            .user(mockUser) // 사용자 설정
            .createdAt(LocalDateTime.now())
            .build();

    // Mock Repository 설정
    given(diaryRepository.findByIsPublicIsTrue(any(Pageable.class)))
        .willReturn(List.of(oldDiary, recentDiary));
    given(avatarPostRepository.findAll(any(Pageable.class)))
        .willReturn(new PageImpl<>(List.of(recentAvatarPost)));

    // when
    List<FeedItemResponse> feed = feedService.getFeed(userUuid, null);

    // then
    assertThat(feed).hasSize(3);
    // 최신순으로 정렬되었는지 확인 (가장 최신인 recentDiary가 첫 번째여야 함)
    assertThat(feed.get(0).getCreatedAt()).isEqualTo(recentDiary.getCreatedAt());
    assertThat(feed.get(1).getCreatedAt()).isEqualTo(recentAvatarPost.getCreatedAt());
    assertThat(feed.get(2).getCreatedAt()).isEqualTo(oldDiary.getCreatedAt());

    verify(diaryRepository).findByIsPublicIsTrue(any(Pageable.class));
    verify(avatarPostRepository).findAll(any(Pageable.class));
    verify(followRepository, never()).findByFollower(any(User.class)); // following 로직은 실행되지 않아야 함
  }

  @Test
  @DisplayName("피드 조회 성공 - 'following' 필터 적용")
  void getFeed_withFollowingFilter_returnsFeedOfFollowedUsers() {
    // given
    User currentUser = User.builder().uuid(UUID.randomUUID()).build();
    User followingUser1 = User.builder().uuid(UUID.randomUUID()).build();
    User notFollowingUser = User.builder().uuid(UUID.randomUUID()).build();

    Follow follow = Follow.builder().follower(currentUser).following(followingUser1).build();

    Diary followedUserDiary =
        Diary.builder().user(followingUser1).createdAt(LocalDateTime.now()).build();
    // 일부러 팔로우하지 않은 사람의 게시물도 생성
    Diary notFollowedUserDiary =
        Diary.builder().user(notFollowingUser).createdAt(LocalDateTime.now().minusDays(1)).build();

    // Mock Repository 설정
    given(userRepository.findByUuid(currentUser.getUuid())).willReturn(Optional.of(currentUser));
    given(followRepository.findByFollower(currentUser)).willReturn(List.of(follow));

    // 💡 3. `followingUsers` 대신 `anyList()` 사용
    given(diaryRepository.findByUserInAndIsPublicIsTrue(anyList(), any(Pageable.class)))
        .willReturn(List.of(followedUserDiary));
    // 💡 4. avatarPostRepository도 동일하게 수정
    given(avatarPostRepository.findByUserIn(anyList(), any(Pageable.class)))
        .willReturn(Collections.emptyList());

    // when
    List<FeedItemResponse> feed = feedService.getFeed(currentUser.getUuid(), "following");

    // then
    assertThat(feed).hasSize(1);
    assertThat(feed.get(0).getCreatedAt()).isEqualTo(followedUserDiary.getCreatedAt());

    verify(userRepository).findByUuid(currentUser.getUuid());
    verify(followRepository).findByFollower(currentUser);
    verify(diaryRepository).findByUserInAndIsPublicIsTrue(anyList(), any(Pageable.class));
    verify(avatarPostRepository).findByUserIn(anyList(), any(Pageable.class));
    verify(diaryRepository, never())
        .findByIsPublicIsTrue(any(Pageable.class)); // 전체 조회 로직은 실행되지 않아야 함
  }

  @Test
  @DisplayName("피드 조회 실패 - 'following' 필터 적용 시 사용자를 찾을 수 없음")
  void getFeed_withFollowingFilter_throwsUserNotFoundException() {
    // given
    UUID nonExistentUserUuid = UUID.randomUUID();
    given(userRepository.findByUuid(nonExistentUserUuid)).willReturn(Optional.empty());

    // when & then
    assertThrows(
        UserNotFoundException.class,
        () -> feedService.getFeed(nonExistentUserUuid, "following"),
        "사용자를 찾을 수 없습니다.");

    verify(followRepository, never()).findByFollower(any()); // 사용자를 못찾았으니 다음 로직은 실행되면 안됨
  }

  @Test
  @DisplayName("피드 조회 성공 - 게시물이 하나도 없을 때 빈 리스트 반환")
  void getFeed_noPosts_returnsEmptyList() {
    // given
    UUID userUuid = UUID.randomUUID();
    given(diaryRepository.findByIsPublicIsTrue(any(Pageable.class)))
        .willReturn(Collections.emptyList());
    given(avatarPostRepository.findAll(any(Pageable.class)))
        .willReturn(new PageImpl<>(Collections.emptyList()));

    // when
    List<FeedItemResponse> feed = feedService.getFeed(userUuid, null);

    // then
    assertThat(feed).isNotNull();
    assertThat(feed).isEmpty();
  }
}
