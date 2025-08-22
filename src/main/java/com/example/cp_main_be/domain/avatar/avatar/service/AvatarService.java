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

  // [수정] 새로운 아바타 생성 로직 구현
  public void createAvatar(Long userId, Long masterId, String nickname) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));

    AvatarMaster master =
        avatarMasterRepository
            .findById(masterId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_FOUND));

    // TODO: 사용자가 이미 해당 종류의 아바타를 가지고 있는지 확인하는 로직 추가 가능

    Avatar newAvatar = Avatar.builder().user(user).avatarMaster(master).nickname(nickname).build();

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
