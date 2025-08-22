package com.example.cp_main_be.domain.member.user.service;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import com.example.cp_main_be.domain.avatar.avatar.domain.AvatarMaster;
import com.example.cp_main_be.domain.avatar.avatar.domain.repository.AvatarRepository;
import com.example.cp_main_be.domain.member.level.service.LevelService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.user.dto.request.AvatarChangeRequest;
import com.example.cp_main_be.domain.member.user.dto.response.UserRegisterResponse;
import com.example.cp_main_be.global.exception.AvatarNotFoundException;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.util.List;
import java.util.UUID;
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

  public void addExperience(Long actorId, int points) {
    User user = userRepository.findById(actorId).get();
    user.addExperience(points);
    levelService.checkLevelUp(user);
  }

  public void updateAvatar(User user, AvatarChangeRequest request, Long avatarId) {
    if(user == null) return;
    Avatar avatar = avatarRepository.findById(avatarId)
            .orElseThrow(() -> new AvatarNotFoundException("아바타를 찾을 수 없습니다."));
    if(avatar == null) return;
    if(request.getNewAvatarUrl() != null)
    {
      avatar.getAvatarMaster().setDefaultImageUrl(request.getNewAvatarUrl());
    }
    if(request.getNewAvatarName() != null)
    {
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
    if (principal instanceof String) {
      String uuidString = (String) principal;
      UUID userUuid = UUID.fromString(uuidString);
      return userRepository
          .findByUuid(userUuid)
          .orElseThrow(() -> new IllegalArgumentException("현재 로그인한 사용자를 찾을 수 없습니다."));
    }

    throw new IllegalArgumentException("인증 정보를 찾을 수 없습니다.");
  }
}
