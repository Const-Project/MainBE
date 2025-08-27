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
import com.example.cp_main_be.global.exception.AvatarNotFoundException;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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

  private final UserRepository userRepository;
  private final LevelService levelService;
  private final AvatarRepository avatarRepository;
  private final FriendWateringLogRepository friendWateringLogRepository;
  private final FollowRepository followRepository;

  public void addExperience(Long actorId, int points) {
    User user =
        userRepository
            .findById(actorId)
            .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()));
    user.addExperience(points);
    levelService.checkLevelUp(user);
  }

  public void updateAvatar(User user, AvatarChangeRequest request, Long avatarId) {
    if (user == null) return;
    Avatar avatar =
        avatarRepository
            .findById(avatarId)
            .orElseThrow(() -> new AvatarNotFoundException("아바타를 찾을 수 없습니다."));
    if (avatar == null) return;
    if (request.getNewAvatarUrl() != null) {
      avatar.getAvatarMaster().setDefaultImageUrl(request.getNewAvatarUrl());
    }
    if (request.getNewAvatarName() != null) {
      avatar.setNickname(request.getNewAvatarName());
    }
    avatarRepository.save(avatar);
  }

  public void updateNickname(User user, String newNickname) {

    user.updateProfile(newNickname, null);
    userRepository.save(user);
  }

  public void saveUser(User user) {
    this.userRepository.save(user);
  }

  public void deleteUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
    userRepository.delete(user);
  }

  public User findUserById(Long id) {
    return this.userRepository
        .findById(id)
        .orElseThrow(() -> new UserNotFoundException("해당 ID의 사용자를 찾을 수 없습니다 : " + id));
  }

  public User findUserByUuid(UUID uuid) {
    return this.userRepository
        .findByUuid(uuid)
        .orElseThrow(() -> new UserNotFoundException("해당 UUID의 사용자를 찾을 수 없습니다 : " + uuid));
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
          .findByUuid(userUuid)
          .orElseThrow(() -> new IllegalArgumentException("현재 로그인한 사용자를 찾을 수 없습니다."));
    }

    throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
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
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

    return managedUser.getGardens().stream()
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
    User currentUser =
        userRepository
            .findById(currentUserId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));
    User profileUser =
        userRepository
            .findById(profileUserId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));

    // [추가] 프로필 이미지 URL을 첫 번째 텃밭의 아바타 이미지로 설정
    String profileImageUrl =
        profileUser.getGardens().stream()
            .min(Comparator.comparing(Garden::getSlotNumber)) // 슬롯 번호가 가장 낮은 텃밭 찾기
            .map(Garden::getAvatar) // 해당 텃밭의 아바타 가져오기
            .filter(Objects::nonNull) // 아바타가 null이 아닌 경우 필터링
            .map(avatar -> avatar.getAvatarMaster().getDefaultImageUrl()) // 아바타의 이미지 URL 가져오기
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

    // 2. 남에게 물 줄 수 있는 남은 횟수 계산 (현재 접속 유저 기준)
    LocalDateTime startOfWateringDay = getStartOfCurrentWateringDay();
    int todayWateringCountForOthers =
        friendWateringLogRepository.countByWaterGiverAndWateredAtAfter(
            currentUser, startOfWateringDay);
    Long leftWaterCountForOthers =
        (long) (3 - todayWateringCountForOthers); // MAX_FRIEND_WATERING_PER_DAY = 3

    // 3. 프로필 주인의 정원 목록 및 물주기 가능 여부 계산
    List<UserGardenDetailResponse> userGardens =
        profileUser.getGardens().stream()
            .map(
                garden -> {
                  // 현재 접속 유저가 이 정원에 오늘 물을 줄 수 있는지 여부
                  boolean isWateringAbleByMe = false;
                  // 조건: 1) 아직 오늘 남에게 물 줄 수 있는 횟수가 남아있어야 하고 (leftWaterCountForOthers > 0)
                  //       2) 오늘 이 정원에 내가 물을 준 적이 없어야 한다.
                  if (leftWaterCountForOthers > 0) {
                    boolean alreadyWateredByMe =
                        friendWateringLogRepository
                            .existsByWaterGiverAndWateredGardenAndWateredAtAfter(
                                currentUser, garden, startOfWateringDay);
                    isWateringAbleByMe = !alreadyWateredByMe;
                  }

                  HomeResponseDto.AvatarInfo avatarInfoForGarden =
                      HomeResponseDto.AvatarInfo.builder()
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
        .leftWaterCountForOthers(leftWaterCountForOthers)
        .userGardens(userGardens)
        .build();
  }

  // GardenService에 있던 private 메서드를 가져오거나 공통 유틸 클래스로 분리해야 합니다.
  private LocalDateTime getStartOfCurrentWateringDay() {
    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    LocalDateTime todayNoon = now.toLocalDate().atTime(12, 0);

    if (now.isBefore(todayNoon)) {
      return todayNoon.minusDays(1);
    } else {
      return todayNoon;
    }
  }
}
