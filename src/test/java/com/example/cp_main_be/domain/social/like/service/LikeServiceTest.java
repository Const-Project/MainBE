package com.example.cp_main_be.domain.social.like.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

import com.example.cp_main_be.domain.member.notification.domain.NotificationType;
import com.example.cp_main_be.domain.member.notification.service.NotificationService;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.social.like.domain.Like;
import com.example.cp_main_be.domain.social.like.domain.repository.LikeRepository;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

  @Mock private LikeRepository likeRepository;
  @Mock private UserRepository userRepository;
  @Mock private DiaryRepository diaryRepository;
  @Mock private NotificationService notificationService;
  @Mock private AvatarPostRepository avatarPostRepository;

  @InjectMocks private LikeService likeService;

  //  @DisplayName("좋아요 추가 성공")
  //  @Test
  //  void addLike_success() {
  //    // given
  //    Long userId = 1L;
  //    Long targetId = 10L;
  //    String targetType = "DIARY";
  //    User user = User.builder().id(userId).username("testuser").build();
  //
  //    given(userRepository.findById(userId)).willReturn(Optional.of(user));
  //    given(likeRepository.existsByUserAndTargetIdAndTargetType(user, targetId, targetType))
  //        .willReturn(false);
  //
  //    // when
  //    likeService.addLike(userId, targetId, targetType);
  //
  //    // then
  //    verify(likeRepository).save(any(Like.class));
  //  }

  @DisplayName("좋아요 추가 실패 - 이미 좋아요를 눌렀음")
  @Test
  void addLike_fail_alreadyLiked() {
    // given
    Long userId = 1L;
    Long targetId = 10L;
    String targetType = "DIARY";
    User user = User.builder().id(userId).username("testuser").build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(likeRepository.existsByUserAndTargetIdAndTargetType(user, targetId, targetType))
        .willReturn(true);

    // when & then
    Assertions.assertThrows(
        RuntimeException.class, () -> likeService.addLike(userId, targetId, targetType));
    verify(likeRepository, org.mockito.Mockito.never()).save(any(Like.class));
  }

  @DisplayName("좋아요 추가 실패 - 사용자 없음")
  @Test
  void addLike_fail_userNotFound() {
    // given
    Long userId = 1L;
    Long targetId = 10L;
    String targetType = "DIARY";

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    Assertions.assertThrows(
        UserNotFoundException.class, () -> likeService.addLike(userId, targetId, targetType));
    verify(likeRepository, org.mockito.Mockito.never()).save(any(Like.class));
  }

  //  @DisplayName("좋아요 삭제 성공")
  //  @Test
  //  void removeLike_success() {
  //    // given
  //    Long userId = 1L;
  //    Long targetId = 10L;
  //    String targetType = "DIARY";
  //    User user = User.builder().id(userId).username("testuser").build();
  //    Like like = Like.builder().user(user).targetId(targetId).targetType(targetType).build();
  //
  //    given(userRepository.findById(userId)).willReturn(Optional.of(user));
  //    given(likeRepository.findByUserAndTargetIdAndTargetType(user, targetId, targetType))
  //        .willReturn(Optional.of(like));
  //
  //    // when
  //    likeService.removeLike(userId, targetId, targetType);
  //
  //    // then
  //    verify(likeRepository).delete(like);
  //  }

  @DisplayName("좋아요 삭제 실패 - 좋아요를 찾을 수 없음")
  @Test
  void removeLike_fail_likeNotFound() {
    // given
    Long userId = 1L;
    Long targetId = 10L;
    String targetType = "DIARY";
    User user = User.builder().id(userId).username("testuser").build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(likeRepository.findByUserAndTargetIdAndTargetType(user, targetId, targetType))
        .willReturn(Optional.empty());

    // when & then
    Assertions.assertThrows(
        RuntimeException.class, () -> likeService.removeLike(userId, targetId, targetType));
    verify(likeRepository, org.mockito.Mockito.never()).delete(any(Like.class));
  }

  @DisplayName("좋아요 삭제 실패 - 사용자 없음")
  @Test
  void removeLike_fail_userNotFound() {
    // given
    Long userId = 1L;
    Long targetId = 10L;
    String targetType = "DIARY";

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    Assertions.assertThrows(
        UserNotFoundException.class, () -> likeService.removeLike(userId, targetId, targetType));
    verify(likeRepository, org.mockito.Mockito.never()).delete(any(Like.class));
  }

  @DisplayName("일기 좋아요 추가 성공")
  @Test
  void addLike_toDiary_Success() {
    // given
    Long userId = 1L;
    Long receiverId = 2L;
    Long targetId = 10L;
    User user = User.builder().id(userId).username("likingUser").build();
    User receiver = User.builder().id(receiverId).username("ownerUser").build();

    String targetType = "DIARY";
    Diary diary = Diary.builder().id(targetId).user(receiver).build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(likeRepository.existsByUserAndTargetIdAndTargetType(user, targetId, targetType))
        .willReturn(false);
    given(diaryRepository.findById(targetId)).willReturn(Optional.of(diary));

    // when
    int likeCount = likeService.addLike(userId, targetId, targetType);

    // then
    assertThat(likeCount).isEqualTo(1);
    then(diaryRepository).should().findById(targetId);
    then(likeRepository).should().save(any(Like.class));
    then(notificationService)
        .should()
        .send(receiver, user, NotificationType.DIARY_LIKE, "/diaries/" + targetId);
  }

  @DisplayName("아바타 포스트 좋아요 추가 성공")
  @Test
  void addLike_toAvatarPost_Success() {
    // given
    Long userId = 1L;
    Long receiverId = 2L;
    Long targetId = 10L;
    User user = User.builder().id(userId).username("likingUser").build();
    User receiver = User.builder().id(receiverId).username("ownerUser").build();

    String targetType = "AVATAR_POST";
    AvatarPost avatarPost = AvatarPost.builder().id(targetId).user(receiver).build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(likeRepository.existsByUserAndTargetIdAndTargetType(user, targetId, targetType))
        .willReturn(false);
    given(avatarPostRepository.findById(targetId)).willReturn(Optional.of(avatarPost));

    // when
    int likeCount = likeService.addLike(userId, targetId, targetType);

    // then
    assertThat(likeCount).isEqualTo(1);
    then(avatarPostRepository).should().findById(targetId);
    then(likeRepository).should().save(any(Like.class));
    then(notificationService)
        .should()
        .send(receiver, user, NotificationType.AVATAR_POST_LIKE, "/avatar-posts/" + targetId);
  }

  @DisplayName("일기 좋아요 취소 성공")
  @Test
  void removeLike_fromDiary_Success() {
    // given
    Long userId = 1L;
    Long receiverId = 2L;
    Long targetId = 10L;
    User user = User.builder().id(userId).username("likingUser").build();
    User receiver = User.builder().id(receiverId).username("ownerUser").build();

    String targetType = "diary"; // 현재 구현은 소문자를 사용
    Like like =
        Like.builder().id(100L).user(user).targetId(targetId).targetType(targetType).build();
    Diary diary = Diary.builder().id(targetId).user(receiver).likeCount(1).build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(likeRepository.findByUserAndTargetIdAndTargetType(user, targetId, targetType))
        .willReturn(Optional.of(like));
    given(diaryRepository.findById(targetId)).willReturn(Optional.of(diary));

    // when
    int likeCount = likeService.removeLike(userId, targetId, targetType);

    // then
    assertThat(likeCount).isZero();
    then(likeRepository).should().delete(like);
  }

  @DisplayName("아바타 포스트 좋아요 취소 성공")
  @Test
  void removeLike_fromAvatarPost_Success() {
    // given
    Long userId = 1L;
    Long receiverId = 2L;
    Long targetId = 10L;
    User user = User.builder().id(userId).username("likingUser").build();
    User receiver = User.builder().id(receiverId).username("ownerUser").build();

    String targetType = "avatar_post"; // 현재 구현은 소문자를 사용
    Like like =
        Like.builder().id(100L).user(user).targetId(targetId).targetType(targetType).build();
    AvatarPost avatarPost = AvatarPost.builder().id(targetId).user(receiver).likeCount(1).build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(likeRepository.findByUserAndTargetIdAndTargetType(user, targetId, targetType))
        .willReturn(Optional.of(like));
    given(avatarPostRepository.findById(targetId)).willReturn(Optional.of(avatarPost));

    // when
    int likeCount = likeService.removeLike(userId, targetId, targetType);

    // then
    assertThat(likeCount).isZero();
    then(likeRepository).should().delete(like);
  }
}
