package com.example.cp_main_be.domain.member.user.service;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import com.example.cp_main_be.domain.avatar.avatar.domain.repository.AvatarRepository;
import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.wateringlog.domain.repository.FriendWateringLogRepository;
import com.example.cp_main_be.domain.home.HomeResponseDto;
import com.example.cp_main_be.domain.member.level.service.LevelService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.user.dto.request.AvatarChangeRequest;
import com.example.cp_main_be.domain.member.user.dto.response.FollowStatus;
import com.example.cp_main_be.domain.member.user.dto.response.UserGardenDetailResponse;
import com.example.cp_main_be.domain.member.user.dto.response.UserProfileResponse;
import com.example.cp_main_be.domain.member.user.dto.response.UserRegisterResponse;
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
  private final LevelService levelService;
  private final AvatarRepository avatarRepository;
  private final FriendWateringLogRepository friendWateringLogRepository;
  private final FollowRepository followRepository;

  public void addExperience(Long actorId, int points) {
    User user =
        userRepository
            .findById(actorId) // ID로 최신 유저 정보를 조회합니다.
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
    user.addExperience(points);
    levelService.checkLevelUp(user);
  }

  public void updateAvatar(User user, AvatarChangeRequest request, Long avatarId) {
    // [수정] 불필요한 null 체크를 제거하고, 일관된 예외 처리를 사용합니다.
    if (user == null) {
      throw new CustomApiException(ErrorCode.INVALID_TOKEN, "사용자 정보를 찾을 수 없습니다.");
    }
    Avatar avatar =
        avatarRepository
            .findById(avatarId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.AVATAR_NOT_FOUND));

    // [버그 수정] AvatarMaster(원본)가 아닌 Avatar(개별 인스턴스)의 imageUrl을 변경해야 합니다.
    // Avatar 엔티티에 imageUrl 필드가 있어야 합니다.
    if (request.getNewAvatarUrl() != null) {
      // avatar.getAvatarMaster().setDefaultImageUrl(request.getNewAvatarUrl()); // 절대 이렇게 하면 안됩니다.
      avatar.setImageUrl(request.getNewAvatarUrl()); // 이렇게 수정해야 합니다.
    }
    if (request.getNewAvatarName() != null) {
      avatar.setNickname(request.getNewAvatarName());
    }
    avatarRepository.save(avatar);
  }

  public void updateNickname(User user, String newNickname) {
    // [수정] stale한 user 객체 대신, ID로 최신 정보를 조회해서 사용합니다.
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
            .findById(userId) // ID로 최신 유저 정보를 조회합니다.
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
    userRepository.delete(user);
  }

  public User findUserById(Long id) {
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

  public UserRegisterResponse.LevelStatusResponseDTO getLevel(User user) {

    return UserRegisterResponse.LevelStatusResponseDTO.builder()
        .level(user.getLevel())
        .experience(user.getExperience())
        .build();
  }

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Object principal = authentication.getPrincipal();

    // Principal이 User 객체인 경우 (현재 JWT 필터에서 이렇게 저장함)
    if (principal instanceof User) {
      return (User) principal;
    }

    // Principal이 String(UUID)인 경우 (백업 처리)
    if (principal instanceof String uuidString) {
      UUID userUuid = UUID.fromString(uuidString);
      return userRepository
          .findByUuid(userUuid) // UUID로 최신 유저 정보를 조회합니다.
          .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
    }

    throw new CustomApiException(ErrorCode.INVALID_TOKEN, "인증 정보를 찾을 수 없습니다.");
  }

  /**
   * 사용자의 모든 텃밭 ID 목록을 슬롯 번호 순으로 정렬하여 반환합니다.
   *
   * @param user 현재 로그인한 사용자
   * @return 정렬된 텃밭 ID 목록
   */
  @Transactional(readOnly = true)
  public List<Long> getMyGardenIds(User user) {
    // LazyInitializationException을 방지하기 위해 Fetch Join으로 User와 gardens를 함께 조회
    User managedUser =
        userRepository
            .findByIdWithGardens(user.getId())
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

    return managedUser.getGardens().stream()
        .filter(garden -> !garden.isLocked()) // [추가] 잠겨있지 않은(isLocked=false) 텃밭만 필터링합니다.
        .sorted(Comparator.comparing(Garden::getSlotNumber))
        .map(Garden::getId)
        .collect(Collectors.toList());
  }

  /**
   * 특정 유저의 프로필 정보를 조회합니다.
   *
   * @param currentUserId 현재 로그인한 유저(프로필을 보고 있는 사람)의 ID
   * @param profileUserId 프로필의 주인 ID
   * @return UserProfileResponse DTO
   */
  public UserProfileResponse getUserProfile(Long currentUserId, Long profileUserId) {
    // 현재 로그인한 유저
    User currentUser =
        userRepository
            .findById(currentUserId) // ID로 최신 유저 정보를 조회합니다.
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));
    // 프로필 조회할 유저
    User profileUser =
        userRepository
            .findByIdWithGardensAndAvatars(profileUserId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

    // [수정] 프로필 이미지 URL을 사용자의 첫 번째 아바타 이미지로 설정
    String profileImageUrl =
        profileUser.getGardens().stream()
            .min(Comparator.comparing(Garden::getSlotNumber)) // 슬롯 번호가 가장 낮은 텃밭 찾기
            .map(Garden::getAvatar) // 해당 텃밭의 아바타 가져오기
            .filter(Objects::nonNull) // 아바타가 null이 아닌 경우 필터링
            .map(
                avatar -> {
                  // AI 아바타처럼 Avatar에 직접 저장된 고유 imageUrl이 있다면 그것을 우선 사용합니다.
                  if (avatar.getImageUrl() != null && !avatar.getImageUrl().isBlank()) {
                    return avatar.getImageUrl();
                  }
                  // 없다면, AvatarMaster에 정의된 기본 이미지를 사용합니다.
                  return avatar.getAvatarMaster().getDefaultImageUrl();
                })
            .orElse(profileUser.getProfileImageUrl()); // 텃밭/아바타가 없으면 기존 프로필 이미지 사용

    // 1. 팔로우 상태 확인
    // currentUserId -> profileUserId 팔로우 여부
    boolean isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, profileUser);
    // profileUserId -> currentUserId 팔로우 여부 (맞팔 여부 확인용)
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

    // 2. 남에게 물 줄 수 있는 남은 횟수 계산
    LocalDateTime startOfWateringDay = TimeUtil.getStartOfCurrentWateringDay();

    // 2-1. 프로필 주인이 남에게 물을 줄 수 있는 남은 횟수 (응답 DTO용)
    int profileUserWateringCount =
        friendWateringLogRepository.countByWaterGiverAndWateredAtAfter(
            profileUser, startOfWateringDay);
    long leftWaterCountForProfileUser =
        Math.max(0, (long) MAX_FRIEND_WATERING_PER_DAY - profileUserWateringCount);

    // 2-2. 현재 접속 유저가 남에게 물을 줄 수 있는 남은 횟수 (물주기 가능 여부 판단용)
    int currentUserWateringCount =
        friendWateringLogRepository.countByWaterGiverAndWateredAtAfter(
            currentUser, startOfWateringDay);
    long leftWaterCountForCurrentUser =
        Math.max(0, (long) MAX_FRIEND_WATERING_PER_DAY - currentUserWateringCount);

    // [성능 개선] N+1 문제를 해결하기 위해, 오늘 내가 물 준 정원 ID 목록을 한 번에 조회합니다.
    Set<Long> wateredGardenIds =
        friendWateringLogRepository.findWateredGardenIdsByGiverAndDate(
            currentUser.getId(), startOfWateringDay);

    // 3. 프로필 주인의 정원 목록 및 물주기 가능 여부 계산
    List<UserGardenDetailResponse> userGardens =
        profileUser.getGardens().stream()
            .filter(garden -> !garden.isLocked()) // isLocked가 false인 텃밭만 가져옵니다.
            .sorted(Comparator.comparing(Garden::getSlotNumber))
            .map(
                garden -> {
                  // DB를 반복 조회하는 대신, 미리 조회한 Set에서 확인하여 성능을 개선합니다.
                  // 로그인한 유저가 해당 정원에 물을 아직 안줬고 횟수가 남았다면 true
                  boolean alreadyWateredByMe = wateredGardenIds.contains(garden.getId());
                  boolean isWateringAbleByMe =
                      leftWaterCountForCurrentUser > 0 && !alreadyWateredByMe;

                  HomeResponseDto.AvatarInfo avatarInfoForGarden =
                      HomeResponseDto.AvatarInfo.builder()
                          // 아바타가 없는 텃밭이 있을 수 있는 예외 케이스를 방어합니다.
                          .avatarId(garden.getAvatar() != null ? garden.getAvatar().getId() : null)
                          .avatarName(garden.getAvatar().getNickname())
                          .avatarImageUrl(garden.getAvatar().getAvatarMaster().getDefaultImageUrl())
                          .build();

                  // GardenResponse 대신 UserGardenDetailResponse를 빌드
                  return UserGardenDetailResponse.builder()
                      .gardenId(garden.getId())
                      .avatarInfo(avatarInfoForGarden)
                      .isWateringAbleByMe(isWateringAbleByMe)
                      .build();
                })
            .collect(Collectors.toList());

    // 4. 최종 UserProfileResponse 빌드
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
