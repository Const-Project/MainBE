package com.example.cp_main_be.domain.avatar.service;

import com.example.cp_main_be.domain.avatar.domain.Avatar;
import com.example.cp_main_be.domain.avatar.domain.repository.AvatarRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AvatarService {

  private final AvatarRepository avatarRepository;

  public void save(Avatar avatar) {
    avatarRepository.save(avatar);
  }

  public List<Avatar> getAllAvatar() {
    return avatarRepository.findAll();
  }

  public Avatar findAvatarById(Long avatarId) {
    Optional<Avatar> avatarOptional = avatarRepository.findById(avatarId);
    return avatarOptional.orElse(null);
  }

  // 기본 아바타 목록을 가져오는 메서드 추가
  public List<Avatar> findAllDefaultAvatars() {
    return avatarRepository.findByIsDefaultAvatarTrue();
  }

  // 특정 유저가 생성한 아바타 목록을 가져오는 메서드 (선택 사항)
  public List<Avatar> findUserOwnedAvatars(Long userId) {
    return avatarRepository.findByUserIdAndIsDefaultAvatarFalse(userId);
  }
}
