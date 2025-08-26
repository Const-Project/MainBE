package com.example.cp_main_be.domain.avatar.avatar.service;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import com.example.cp_main_be.domain.avatar.avatar.domain.AvatarMaster;
import com.example.cp_main_be.domain.avatar.avatar.domain.repository.AvatarMasterRepository;
import com.example.cp_main_be.domain.avatar.avatar.domain.repository.AvatarRepository;
import com.example.cp_main_be.domain.member.notification.domain.NotificationType;
import com.example.cp_main_be.domain.member.notification.service.NotificationService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AvatarService {

  private final AvatarRepository avatarRepository;
  private final UserRepository userRepository;
  private final AvatarMasterRepository avatarMasterRepository; // [추가] AvatarMaster 조회 위해 주입
  private final NotificationService notificationService;

  private static final Long AI_AVATAR_MASTER_ID = 9999L;

  // [수정] 새로운 아바타 생성 로직 구현
  public void createAvatar(Long userId, String nickname, String imageUrl, Long masterId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));

    AvatarMaster master;
    if (masterId != null) {
      // 1. 기존 목록에서 선택한 경우: 전달받은 masterId로 AvatarMaster를 찾습니다.
      master =
          avatarMasterRepository
              .findById(masterId + 2)
              .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND)); // 예외 유형 구체화
    } else {
      // 2. AI로 생성한 경우: 약속된 AI_AVATAR_MASTER_ID로 AvatarMaster를 찾습니다.
      master =
          avatarMasterRepository
              .findById(AI_AVATAR_MASTER_ID)
              .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));
    }

    Avatar newAvatar =
        Avatar.builder()
            .user(user)
            .nickname(nickname)
            .imageUrl(imageUrl)
            .avatarMaster(master) // 찾은 master를 설정합니다.
            .build();

    avatarRepository.save(newAvatar);
  }

  @Transactional(readOnly = true)
  public Avatar findAvatarById(Long avatarId) {
    return avatarRepository
        .findById(avatarId)
        .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));
  }

  public void givePollen(Long senderId, Long avatarId) {
    User sender =
        userRepository
            .findById(senderId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));

    Avatar avatar = findAvatarById(avatarId);
    User receiver = avatar.getUser();

    // TODO: 꽃가루 관련 비즈니스 로직 추가

    notificationService.send(
        receiver, sender, NotificationType.POLLEN, "/garden/" + receiver.getId());
  }
}
