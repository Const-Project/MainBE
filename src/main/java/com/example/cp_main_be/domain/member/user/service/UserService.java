package com.example.cp_main_be.domain.member.user.service;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import com.example.cp_main_be.domain.avatar.avatar.domain.repository.AvatarRepository;
import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.wateringlog.domain.repository.FriendWateringLogRepository;
import com.example.cp_main_be.domain.home.HomeResponseDto;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.user.dto.request.AvatarChangeRequest;
import com.example.cp_main_be.domain.member.user.dto.response.FollowStatus;
import com.example.cp_main_be.domain.member.user.dto.response.LevelStatusResponseDto;
import com.example.cp_main_be.domain.member.user.dto.response.UserGardenDetailResponse;
import com.example.cp_main_be.domain.member.user.dto.response.UserProfileResponse;
import com.example.cp_main_be.domain.mission.wishTree.WishTree;
import com.example.cp_main_be.domain.mission.wishTree.WishTreeService; // [추가] WishTreeService 임포트
import com.example.cp_main_be.domain.mission.wishTree.WishTreeStage;
import com.example.cp_main_be.domain.social.follow.domain.repository.FollowRepository;
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
  private final WishTreeService wishTreeService; // [추가] WishTreeService 주입

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
    userRepository.save(user);
  }

  public void saveUser(User user) {
    this.userRepository.save(user);
  }

  public void deleteUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
    userRepository.delete(user);
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
                  return avatar.getAvatarMaster().getDefaultImageUrl();
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

    int profileUserWateringCount =
        friendWateringLogRepository.countByWaterGiverAndWateredAtAfter(
            profileUser, startOfWateringDay);
    long leftWaterCountForProfileUser =
        Math.max(0, (long) MAX_FRIEND_WATERING_PER_DAY - profileUserWateringCount);

    int currentUserWateringCount =
        friendWateringLogRepository.countByWaterGiverAndWateredAtAfter(
            currentUser, startOfWateringDay);
    long leftWaterCountForCurrentUser =
        Math.max(0, (long) MAX_FRIEND_WATERING_PER_DAY - currentUserWateringCount);

    Set<Long> wateredGardenIds =
        friendWateringLogRepository.findWateredGardenIdsByGiverAndDate(
            currentUser.getId(), startOfWateringDay);

    List<UserGardenDetailResponse> userGardens =
        profileUser.getGardens().stream()
            .filter(garden -> !garden.isLocked())
            .sorted(Comparator.comparing(Garden::getSlotNumber))
            .map(
                garden -> {
                  boolean alreadyWateredByMe = wateredGardenIds.contains(garden.getId());
                  boolean isWateringAbleByMe =
                      leftWaterCountForCurrentUser > 0 && !alreadyWateredByMe;

                  HomeResponseDto.AvatarInfo avatarInfoForGarden =
                      HomeResponseDto.AvatarInfo.builder()
                          .avatarId(garden.getAvatar().getId())
                          .avatarName(garden.getAvatar().getNickname())
                          .avatarImageUrl(garden.getAvatar().getAvatarMaster().getDefaultImageUrl())
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
        .leftWaterCountForOthers(leftWaterCountForProfileUser)
        .userGardens(userGardens)
        .build();
  }
}
