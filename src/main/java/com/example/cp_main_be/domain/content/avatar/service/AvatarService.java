package com.example.cp_main_be.domain.content.avatar.service;

import com.example.cp_main_be.domain.content.avatar.domain.Avatar;
import com.example.cp_main_be.domain.content.avatar.domain.repository.AvatarRepository;
import com.example.cp_main_be.domain.member.notification.domain.NotificationType;
import com.example.cp_main_be.domain.member.notification.service.NotificationService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AvatarService {

  private final AvatarRepository avatarRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  public void save(Avatar avatar) {
    avatarRepository.save(avatar);
  }

  @Transactional(readOnly = true)
  public List<Avatar> getAllAvatar() {
    return avatarRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Avatar findAvatarById(Long avatarId) {
    return avatarRepository
        .findById(avatarId)
        .orElseThrow(() -> new IllegalArgumentException("아바타를 찾을 수 없습니다."));
  }

  @Transactional(readOnly = true)
  public List<Avatar> findAllDefaultAvatars() {
    return avatarRepository.findByIsDefaultAvatarTrue();
  }

  @Transactional(readOnly = true)
  public List<Avatar> findUserOwnedAvatars(Long userId) {
    return avatarRepository.findByUserIdAndIsDefaultAvatarFalse(userId);
  }

  public void givePollen(Long senderId, Long avatarId) {
    User sender =
        userRepository
            .findById(senderId)
            .orElseThrow(() -> new UserNotFoundException("꽃가루를 보내는 사용자를 찾을 수 없습니다."));

    Avatar avatar = findAvatarById(avatarId);
    User receiver = avatar.getUser();

    // TODO: 꽃가루 관련 비즈니스 로직 추가 (예: 사용자 꽃가루 개수 차감, 아바타 경험치 증가 등)

    // 알림 전송
    notificationService.send(
        receiver, sender, NotificationType.POLLEN, "/garden/" + receiver.getId());
  }
}
