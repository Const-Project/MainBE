package com.example.cp_main_be.domain.user.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.cp_main_be.domain.avatar.avatar.domain.repository.AvatarRepository;
import com.example.cp_main_be.domain.delivery.domain.repository.DeliveryRepository;
import com.example.cp_main_be.domain.garden.wateringlog.domain.repository.FriendWateringLogRepository;
import com.example.cp_main_be.domain.member.auth.domain.repository.RefreshTokenRepository;
import com.example.cp_main_be.domain.member.daily_question.domain.repository.DailyQuestionAnswerRepository;
import com.example.cp_main_be.domain.member.log.domain.repository.UserDailyActivityLogRepository;
import com.example.cp_main_be.domain.member.notification.domain.repository.DeviceTokenRepository;
import com.example.cp_main_be.domain.member.notification.domain.repository.EmitterRepository;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.domain.member.userblock.UserBlockRepository;
import com.example.cp_main_be.domain.mission.diaryimage.domain.DiaryImageRepository;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.repository.UserDailyMissionRepository;
import com.example.cp_main_be.domain.mission.wishTree.WishTreeService;
import com.example.cp_main_be.domain.realquiz.repository.UserQuizRepository;
import com.example.cp_main_be.domain.reports.domain.repository.ReportRepository;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.bookmark.domain.repository.BookmarkRepository;
import com.example.cp_main_be.domain.social.comment.domain.repository.CommentRepository;
import com.example.cp_main_be.domain.social.feed.domain.repository.FeedRepository;
import com.example.cp_main_be.domain.social.follow.domain.repository.FollowRepository;
import com.example.cp_main_be.domain.social.guestbook.domain.repository.GuestbookRepository;
import com.example.cp_main_be.domain.social.like.avatar_post.repository.AvatarPostLikeRepository;
import com.example.cp_main_be.domain.social.like.diary.repository.DiaryLikeRepository;
import com.example.cp_main_be.domain.tracking.domain.repository.TrackingReportViewRepository;
import com.example.cp_main_be.global.common.CustomApiException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private AvatarRepository avatarRepository;
  @Mock private FriendWateringLogRepository friendWateringLogRepository;
  @Mock private FollowRepository followRepository;
  @Mock private UserBlockRepository userBlockRepository;
  @Mock private WishTreeService wishTreeService;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private DeviceTokenRepository deviceTokenRepository;
  @Mock private GuestbookRepository guestbookRepository;
  @Mock private EmitterRepository emitterRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private AvatarPostLikeRepository avatarPostLikeRepository;
  @Mock private AvatarPostRepository avatarPostRepository;
  @Mock private FeedRepository feedRepository;
  @Mock private DiaryLikeRepository diaryLikeRepository;
  @Mock private DiaryImageRepository diaryImageRepository;
  @Mock private BookmarkRepository bookmarkRepository;
  @Mock private DeliveryRepository deliveryRepository;
  @Mock private UserQuizRepository userQuizRepository;
  @Mock private TrackingReportViewRepository trackingReportViewRepository;
  @Mock private UserDailyActivityLogRepository userDailyActivityLogRepository;
  @Mock private DailyQuestionAnswerRepository dailyQuestionAnswerRepository;
  @Mock private UserDailyMissionRepository userDailyMissionRepository;
  @Mock private ReportRepository reportRepository;

  @InjectMocks private UserService userService;

  @DisplayName("deleteUser: 연관 데이터가 있는 사용자 탈퇴 시 모든 FK 정리 호출 후 삭제")
  @Test
  void deleteUser_연관_데이터_FK_모두_정리됨() {
    Long userId = 1L;
    UUID userUuid = UUID.randomUUID();
    User user = User.builder().id(userId).uuid(userUuid).nickname("test").build();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    userService.deleteUser(userId);

    // 독립 데이터 정리
    verify(deliveryRepository).deleteAllByUser(user);
    verify(userQuizRepository).deleteAllByUser(user);
    verify(trackingReportViewRepository).deleteAllByUser(user);
    verify(userDailyActivityLogRepository).deleteAllByUser(user);
    verify(dailyQuestionAnswerRepository).deleteAllByUser(user);
    verify(userDailyMissionRepository).deleteAllByUser(user);
    verify(reportRepository).deleteAllByUser(user);

    // 차단 정보
    verify(userBlockRepository).deleteAllByBlockerUser(user);
    verify(userBlockRepository).deleteAllByBlockedUser(user);

    // 좋아요 및 물주기 (본인이 준 것)
    verify(avatarPostLikeRepository).deleteAllByUser(user);
    verify(diaryLikeRepository).deleteAllByUser(user);
    verify(friendWateringLogRepository).deleteAllByWaterGiver(user);

    // 본인 게시물 관련 (타인이 남긴 데이터 포함)
    verify(bookmarkRepository).deleteAllByAvatarPostUser(user);
    verify(avatarPostLikeRepository).deleteAllByAvatarPostUser(user);
    verify(commentRepository).deleteAllByAvatarPostUser(user);
    verify(commentRepository).deleteAllByWriter(user);
    verify(avatarPostRepository).deleteAllByUser(user);
    verify(feedRepository).deleteAllByUser(user);

    // User cascade(Garden, Diary) 전 선행 정리
    verify(diaryLikeRepository).deleteAllByDiaryUser(user);
    verify(diaryImageRepository).deleteAllByUser(user);
    verify(friendWateringLogRepository).deleteAllByWateredGardenUser(user);

    // 팔로우 및 방명록
    verify(followRepository).deleteAllByFollower(user);
    verify(followRepository).deleteAllByFollowing(user);
    verify(guestbookRepository).deleteAllByWriter(user);
    verify(guestbookRepository).deleteAllByOwner(user);

    // 인증 토큰
    verify(refreshTokenRepository).deleteAllByUserUuid(userUuid);
    verify(deviceTokenRepository).deleteByUser(user);

    // 유저 삭제 및 SSE 정리
    verify(userRepository).delete(user);
    verify(emitterRepository).deleteAllEmitterStartWithId(String.valueOf(userId));
    verify(emitterRepository).deleteAllEventCacheStartWithId(String.valueOf(userId));
  }

  @DisplayName("deleteUser: 존재하지 않는 유저는 예외 발생 후 삭제 미호출")
  @Test
  void deleteUser_유저없음_예외() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.deleteUser(1L)).isInstanceOf(CustomApiException.class);

    verify(userRepository, never()).delete(any(User.class));
  }

  private static <T> T any(Class<T> clazz) {
    return org.mockito.ArgumentMatchers.any(clazz);
  }
}
