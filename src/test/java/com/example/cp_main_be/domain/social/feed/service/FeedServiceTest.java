package com.example.cp_main_be.domain.social.feed.service;

import static org.mockito.BDDMockito.given;

import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private FeedService feedService;

  @DisplayName("피드 조회 성공 - 필터 없음")
  @Test
  void getFeed_success_noFilter() {
    // given
    UUID userUuid = UUID.randomUUID();
    User user = User.builder().uuid(userUuid).username("testuser").build();

    given(userRepository.findByUuid(userUuid)).willReturn(Optional.of(user));

    // when
    List<Object> feedItems = feedService.getFeed(userUuid, null);

    // then
    Assertions.assertNotNull(feedItems);
    // TODO: 실제 피드 데이터가 추가되면 검증 로직 보완
  }

  @DisplayName("피드 조회 성공 - following 필터")
  @Test
  void getFeed_success_followingFilter() {
    // given
    UUID userUuid = UUID.randomUUID();
    User user = User.builder().uuid(userUuid).username("testuser").build();

    given(userRepository.findByUuid(userUuid)).willReturn(Optional.of(user));

    // when
    List<Object> feedItems = feedService.getFeed(userUuid, "following");

    // then
    Assertions.assertNotNull(feedItems);
    // TODO: 실제 팔로우 피드 데이터가 추가되면 검증 로직 보완
  }

  @DisplayName("피드 조회 실패 - 사용자 없음")
  @Test
  void getFeed_fail_userNotFound() {
    // given
    UUID userUuid = UUID.randomUUID();

    given(userRepository.findByUuid(userUuid)).willReturn(Optional.empty());

    // when & then
    Assertions.assertThrows(UserNotFoundException.class, () -> feedService.getFeed(userUuid, null));
  }
}
