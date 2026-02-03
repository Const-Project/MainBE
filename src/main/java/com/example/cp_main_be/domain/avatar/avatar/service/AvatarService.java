package com.example.cp_main_be.domain.avatar.avatar.service;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import com.example.cp_main_be.domain.avatar.avatar.domain.AvatarMaster;
import com.example.cp_main_be.domain.avatar.avatar.domain.repository.AvatarMasterRepository;
import com.example.cp_main_be.domain.avatar.avatar.domain.repository.AvatarRepository;
import com.example.cp_main_be.domain.garden.garden.domain.Garden;
import com.example.cp_main_be.domain.garden.garden.domain.repository.GardenRepository;
import com.example.cp_main_be.domain.member.notification.domain.NotificationType;
import com.example.cp_main_be.domain.member.notification.service.NotificationService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.userblock.UserBlockRepository;
import com.example.cp_main_be.domain.mission.wishTree.WishTreeRepository;
import com.example.cp_main_be.domain.social.avatarpost.service.AvatarPostService;
import com.example.cp_main_be.domain.social.follow.domain.repository.FollowRepository;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AvatarService {

  private static final Long AI_AVATAR_MASTER_ID = 9999L;
  private final AvatarRepository avatarRepository;
  private final UserRepository userRepository;
  private final AvatarMasterRepository avatarMasterRepository; // [추가] AvatarMaster 조회 위해 주입
  private final NotificationService notificationService;
  private final WishTreeRepository wishTreeRepository;
  private final GardenRepository gardenRepository;
  private final AvatarPostService avatarPostService;
  private final UserBlockRepository userBlockRepository;
  private final FollowRepository followRepository;

  // [수정] 새로운 아바타 생성 로직 구현
  public Avatar createAvatar(Long userId, String nickname, String imageUrl, Long masterId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

    AvatarMaster master;
    if (masterId != null) {
      // 1. 기존 목록에서 선택한 경우: 전달받은 masterId로 AvatarMaster를 찾습니다.
      // TODO: 'masterId + 2'와 같은 매직 넘버 로직은 위험합니다.
      // 프론트엔드에서 전달하는 ID와 DB의 ID가 일치하도록 데이터 정합성을 맞추거나,
      // 이 로직에 대한 명확한 주석과 문서화가 필요합니다.
      master =
          avatarMasterRepository
              .findById(masterId)
              .orElseThrow(() -> new CustomApiException(ErrorCode.AVATAR_MASTER_NOT_FOUND));
    } else {
      // 2. AI로 생성한 경우: 약속된 AI_AVATAR_MASTER_ID로 AvatarMaster를 찾습니다.
      master =
          avatarMasterRepository
              .findById(AI_AVATAR_MASTER_ID)
              .orElseThrow(
                  () ->
                      new CustomApiException(
                          ErrorCode.AVATAR_MASTER_NOT_FOUND, "AI 아바타 원본을 찾을 수 없습니다."));
    }

    Avatar newAvatar =
        Avatar.builder()
            .user(user)
            .nickname(nickname)
            .imageUrl(imageUrl)
            .avatarMaster(master) // 찾은 master를 설정합니다.
            .build();

    user.addAvatar(newAvatar);

    avatarRepository.save(newAvatar);

    // 1. 잠겨있지 않으면서(isLocked=false) 아바타가 비어있는(avatar=null) 정원을 찾습니다.
    Garden emptyGarden =
        gardenRepository
            .findFirstByUserAndIsLockedIsFalseAndAvatarIsNullOrderBySlotNumberAsc(user)
            .orElseThrow(
                () -> new CustomApiException(ErrorCode.GARDEN_NOT_FOUND, "배치할 수 있는 빈 정원이 없습니다."));

    // 2. 해당 정원에 새로 생성한 아바타를 배치합니다.
    emptyGarden.updateAvatar(newAvatar);

    avatarPostService.createAvatarPost(newAvatar, user);

    // [추가] 아바타 생성 알림 전송 (Item 4)
    notificationService.send(
        user,
        user,
        NotificationType.AVATAR_CREATED,
        "/garden/" + user.getId(),
        newAvatar.getImageUrl());

    return newAvatar;
  }

  @Transactional(readOnly = true)
  public Avatar findAvatarById(Long avatarId) {
    return avatarRepository
        .findById(avatarId)
        .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND, "해당 아바타를 찾을 수 없습니다."));
  }

  public void givePollen(Long senderId, Long avatarId) {
    User sender =
        userRepository
            .findById(senderId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

    Avatar avatar = findAvatarById(avatarId);
    User receiver = avatar.getUser();

    if (userBlockRepository.existsByBlockerUserAndBlockedUser(receiver, sender)
        || userBlockRepository.existsByBlockerUserAndBlockedUser(sender, receiver)) {
      throw new CustomApiException(ErrorCode.ACCESS_DENIED, "차단된 사용자입니다.");
    }
    if (!sender.getId().equals(receiver.getId())
        && !followRepository.existsByFollowerAndFollowing(sender, receiver)) {
      throw new CustomApiException(ErrorCode.ACCESS_DENIED, "팔로우한 사용자만 꽃가루를 줄 수 있습니다.");
    }

    // TODO: 꽃가루 관련 비즈니스 로직 추가

    notificationService.send(
        receiver, sender, NotificationType.POLLEN, "/garden/" + receiver.getId(), null);
  }
}
