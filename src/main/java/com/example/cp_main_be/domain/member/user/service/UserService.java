package com.example.cp_main_be.domain.member.user.service;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import com.example.cp_main_be.domain.avatar.avatar.domain.repository.AvatarRepository;
import com.example.cp_main_be.domain.delivery.domain.repository.DeliveryRepository;
import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.wateringlog.domain.repository.FriendWateringLogRepository;
import com.example.cp_main_be.domain.home.HomeResponseDto;
import com.example.cp_main_be.domain.member.auth.domain.repository.RefreshTokenRepository;
import com.example.cp_main_be.domain.member.daily_question.domain.repository.DailyQuestionAnswerRepository;
import com.example.cp_main_be.domain.member.log.domain.repository.UserDailyActivityLogRepository;
import com.example.cp_main_be.domain.member.notification.domain.repository.DeviceTokenRepository;
import com.example.cp_main_be.domain.member.notification.domain.repository.EmitterRepository;
import com.example.cp_main_be.domain.member.notification.domain.repository.NotificationRepository;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.user.dto.request.AvatarChangeRequest;
import com.example.cp_main_be.domain.member.user.dto.response.FollowStatus;
import com.example.cp_main_be.domain.member.user.dto.response.LevelStatusResponseDto;
import com.example.cp_main_be.domain.member.user.dto.response.UserGardenDetailResponse;
import com.example.cp_main_be.domain.member.user.dto.response.UserProfileResponse;
import com.example.cp_main_be.domain.member.userblock.UserBlockRepository;
import com.example.cp_main_be.domain.mission.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.mission.diaryimage.domain.DiaryImageRepository;
import com.example.cp_main_be.domain.mission.user_daily_mission.domain.repository.UserDailyMissionRepository;
import com.example.cp_main_be.domain.mission.wishTree.WishTree;
import com.example.cp_main_be.domain.mission.wishTree.WishTreeService;
import com.example.cp_main_be.domain.mission.wishTree.WishTreeStage;
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
import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.util.TimeUtil;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  private static final int MAX_FRIEND_WATERING_PER_DAY = 3;

  private final UserRepository userRepository;
  private final AvatarRepository avatarRepository;
  private final FriendWateringLogRepository friendWateringLogRepository;
  private final FollowRepository followRepository;
  private final UserBlockRepository userBlockRepository;
  private final WishTreeService wishTreeService;
  private final RefreshTokenRepository refreshTokenRepository;
  private final DeviceTokenRepository deviceTokenRepository;
  private final NotificationRepository notificationRepository;
  private final GuestbookRepository guestbookRepository;
  private final EmitterRepository emitterRepository;
  private final CommentRepository commentRepository;
  private final AvatarPostLikeRepository avatarPostLikeRepository;
  private final AvatarPostRepository avatarPostRepository;
  private final FeedRepository feedRepository;
  private final DiaryLikeRepository diaryLikeRepository;
  private final DiaryImageRepository diaryImageRepository;
  private final DiaryRepository diaryRepository;
  private final BookmarkRepository bookmarkRepository;
  private final DeliveryRepository deliveryRepository;
  private final UserQuizRepository userQuizRepository;
  private final TrackingReportViewRepository trackingReportViewRepository;
  private final UserDailyActivityLogRepository userDailyActivityLogRepository;
  private final DailyQuestionAnswerRepository dailyQuestionAnswerRepository;
  private final UserDailyMissionRepository userDailyMissionRepository;
  private final ReportRepository reportRepository;

  // [수정] 경험치 추가 로직을 WishTreeService에 위임
  public void addExperience(Long actorId, Long points) {
    wishTreeService.addPointsToWishTree(actorId, points);
  }

  // [제거] LevelService가 없으므로 getLevel 메서드 삭제

  // [수정] 최종 레벨 도달 시 분기 처리 추가
  @Transactional(readOnly = true)
  public LevelStatusResponseDto getLevelStatus(User user) {
    WishTree wishTree = user.getWishTree();
    if (wishTree == null) {
      throw new CustomApiException(ErrorCode.NOT_FOUND, "소망나무 정보를 찾을 수 없습니다.");
    }

    WishTreeStage currentStage = wishTree.getStage();
    long totalPoints = wishTree.getPoints();

    // [추가] 최종 레벨에 도달했을 경우의 분기 처리
    if (currentStage == WishTreeStage.FINAL) {
      return LevelStatusResponseDto.builder()
          .level(currentStage.getLevel())
          .currentExp(0) // 경험치 바를 채울 필요가 없으므로 0으로 설정
          .requiredExpForNextLevel(0) // 다음 레벨이 없으므로 0으로 설정
          .totalPoints(totalPoints)
          .build();
    }

    long expForCurrentLevelStart = currentStage.getRequiredPoints();
    long expForNextLevelStart = currentStage.getRequiredPointsForNextStage();

    long expInCurrentLevel = totalPoints - expForCurrentLevelStart;
    long expNeededForLevelUp = expForNextLevelStart - expForCurrentLevelStart;

    return LevelStatusResponseDto.builder()
        .level(currentStage.getLevel())
        .currentExp(expInCurrentLevel)
        .requiredExpForNextLevel(expNeededForLevelUp)
        .totalPoints(totalPoints)
        .build();
  }

  public void updateAvatar(User user, AvatarChangeRequest request, Long avatarId) {
    if (user == null) {
      throw new CustomApiException(ErrorCode.INVALID_TOKEN, "사용자 정보를 찾을 수 없습니다.");
    }
    Avatar avatar =
        avatarRepository
            .findById(avatarId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.AVATAR_NOT_FOUND));

    if (!avatar.getUser().getId().equals(user.getId())) {
      throw new CustomApiException(ErrorCode.ACCESS_DENIED, "내 아바타만 수정할 수 있습니다.");
    }

    if (request.getNewAvatarUrl() != null) {
      avatar.setImageUrl(request.getNewAvatarUrl());
    }
    if (request.getNewAvatarName() != null) {
      avatar.setNickname(request.getNewAvatarName());
    }
    avatarRepository.save(avatar);
  }

  public void updateNickname(User user, String newNickname) {
    User managedUser =
        userRepository
            .findById(user.getId())
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
    managedUser.updateProfile(newNickname, null);
    userRepository.save(managedUser);
  }

  public void saveUser(User user) {
    this.userRepository.save(user);
  }

  public void deleteUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

    user.getGardens().forEach(garden -> garden.updateAvatar(null));

    notificationRepository.deleteAllByReceiver(user);
    deliveryRepository.deleteAllByUser(user);
    userQuizRepository.deleteAllByUser(user);
    trackingReportViewRepository.deleteAllByUser(user);
    userDailyActivityLogRepository.deleteAllByUser(user);
    dailyQuestionAnswerRepository.deleteAllByUser(user);
    userDailyMissionRepository.deleteAllByUser(user);
    reportRepository.deleteAllByUser(user);

    userBlockRepository.deleteAllByBlockerUser(user);
    userBlockRepository.deleteAllByBlockedUser(user);

    avatarPostLikeRepository.deleteAllByUser(user);
    diaryLikeRepository.deleteAllByUser(user);
    bookmarkRepository.deleteAllByUser(user);
    friendWateringLogRepository.deleteAllByWaterGiver(user);

    bookmarkRepository.deleteAllByAvatarPostUser(user);
    avatarPostLikeRepository.deleteAllByAvatarPostUser(user);
    commentRepository.deleteAllByAvatarPostUser(user);
    commentRepository.deleteAllByWriter(user);
    avatarPostRepository.deleteAllByUser(user);
    feedRepository.deleteAllByUser(user);

    diaryLikeRepository.deleteAllByDiaryUser(user);
    commentRepository.deleteAllByDiaryUser(user);
    diaryImageRepository.deleteAllByUser(user);
    diaryRepository.deleteAllByUser(user);
    friendWateringLogRepository.deleteAllByWateredGardenUser(user);

    followRepository.deleteAllByFollower(user);
    followRepository.deleteAllByFollowing(user);

    guestbookRepository.deleteAllByWriter(user);
    guestbookRepository.deleteAllByOwner(user);

    refreshTokenRepository.deleteAllByUserUuid(user.getUuid());
    deviceTokenRepository.deleteByUser(user);

    avatarRepository.deleteAllByUser(user);
    userRepository.delete(user);

    String userIdStr = String.valueOf(userId);
    emitterRepository.deleteAllEmitterStartWithId(userIdStr);
    emitterRepository.deleteAllEventCacheStartWithId(userIdStr);
  }

  public User findById(Long id) {
    return this.userRepository
        .findById(id)
        .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
  }

  public User findUserByUuid(UUID uuid) {
    return this.userRepository
        .findByUuid(uuid)
        .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
  }

  public List<User> findAllUsers() {
    return userRepository.findAll();
  }

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Object principal = authentication.getPrincipal();

    if (principal instanceof User) {
      return (User) principal;
    }

    if (principal instanceof String uuidString) {
      UUID userUuid = UUID.fromString(uuidString);
      return userRepository
          .findByUuid(userUuid)
          .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
    }

    throw new CustomApiException(ErrorCode.INVALID_TOKEN, "인증 정보를 찾을 수 없습니다.");
  }

  @Transactional(readOnly = true)
  public List<Long> getMyGardenIds(User user) {
    User managedUser =
        userRepository
            .findByIdWithGardens(user.getId())
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

    return managedUser.getGardens().stream()
        .filter(garden -> !garden.isLocked())
        .sorted(Comparator.comparing(Garden::getSlotNumber))
        .map(Garden::getId)
        .collect(Collectors.toList());
  }

  public UserProfileResponse getUserProfile(Long currentUserId, Long profileUserId) {
    User currentUser =
        userRepository
            .findById(currentUserId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
    User profileUser =
        userRepository
            .findByIdWithGardensAndAvatars(profileUserId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

    if (userBlockRepository.existsByBlockerUserAndBlockedUser(currentUser, profileUser)
        || userBlockRepository.existsByBlockerUserAndBlockedUser(profileUser, currentUser)) {
      throw new CustomApiException(ErrorCode.ACCESS_DENIED, "차단된 사용자입니다.");
    }

    String profileImageUrl =
        profileUser.getGardens().stream()
            .min(Comparator.comparing(Garden::getSlotNumber))
            .map(Garden::getAvatar)
            .filter(Objects::nonNull)
            .map(
                avatar -> {
                  if (avatar.getImageUrl() != null && !avatar.getImageUrl().isBlank()) {
                    return avatar.getImageUrl();
                  }
                  if (avatar.getAvatarMaster() != null) {
                    return avatar.getAvatarMaster().getDefaultImageUrl();
                  }
                  return null;
                })
            .orElse(profileUser.getProfileImageUrl());

    boolean isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, profileUser);
    boolean isFollowedByProfileUser =
        followRepository.existsByFollowerAndFollowing(profileUser, currentUser);

    FollowStatus followStatus;
    if (isFollowing) {
      followStatus = FollowStatus.FOLLOWING;
    } else if (isFollowedByProfileUser) {
      followStatus = FollowStatus.FOLLOW_BACK_POSSIBLE;
    } else {
      followStatus = FollowStatus.NOT_FOLLOWING;
    }

    LocalDateTime startOfWateringDay = TimeUtil.getStartOfCurrentWateringDay();

    long currentUserWateringCountForProfileUser =
        friendWateringLogRepository.countByWaterGiverAndTargetUserAndWateredAtAfter(
            currentUser, profileUser, startOfWateringDay);
    long leftWaterCountForCurrentUser =
        Math.max(0, (long) MAX_FRIEND_WATERING_PER_DAY - currentUserWateringCountForProfileUser);

    List<UserGardenDetailResponse> userGardens =
        profileUser.getGardens().stream()
            .filter(garden -> !garden.isLocked())
            .sorted(Comparator.comparing(Garden::getSlotNumber))
            .map(
                garden -> {
                  boolean isWateringAbleByMe =
                      !profileUser.getId().equals(currentUserId)
                          && isFollowing
                          && leftWaterCountForCurrentUser > 0;

                  HomeResponseDto.AvatarInfo avatarInfoForGarden =
                      HomeResponseDto.AvatarInfo.builder()
                          .avatarId(garden.getAvatar().getId())
                          .avatarName(garden.getAvatar().getNickname())
                          .avatarImageUrl(resolveAvatarImageUrl(garden.getAvatar()))
                          .build();

                  return UserGardenDetailResponse.builder()
                      .gardenId(garden.getId())
                      .avatarInfo(avatarInfoForGarden)
                      .isWateringAbleByMe(isWateringAbleByMe)
                      .build();
                })
            .collect(Collectors.toList());

    return UserProfileResponse.builder()
        .id(profileUser.getId())
        .userNickname(profileUser.getNickname())
        .profileImageUrl(profileImageUrl)
        .followStatus(followStatus)
        // Return the current viewer's remaining friend-watering count for today.
        .leftWaterCountForOthers(leftWaterCountForCurrentUser)
        .userGardens(userGardens)
        .build();
  }

  private String resolveAvatarImageUrl(Avatar avatar) {
    if (avatar.getImageUrl() != null && !avatar.getImageUrl().isBlank()) {
      return avatar.getImageUrl();
    }
    if (avatar.getAvatarMaster() != null) {
      return avatar.getAvatarMaster().getDefaultImageUrl();
    }
    return null;
  }
}
