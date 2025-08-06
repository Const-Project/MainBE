package com.example.cp_main_be.domain.user.service;

import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.user.dto.request.UserRequest;
import com.example.cp_main_be.domain.user.dto.response.UserResponse;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import com.example.cp_main_be.global.jwt.JwtTokenProvider;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;

  public UserResponse registerUser(UserRequest userRequest) {
    User user =
        User.builder()
            .uuid(userRequest.getUuid() != null ? userRequest.getUuid() : UUID.randomUUID())
            .username(userRequest.getUsername())
            .profileImageUrl(userRequest.getAvatarUrl())
            .build();

    userRepository.save(user);

    String accessToken = jwtTokenProvider.generateAccessToken(user.getUuid().toString());
    String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUuid().toString());

    return new UserResponse(
        user.getId(), user.getUsername(), user.getUuid(), accessToken, refreshToken);
  }

  public void updateAvatar(Long userId, String newAvatarUrl) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
    user.setProfileImageUrl(newAvatarUrl);
    userRepository.save(user);
  }

  public void updateNickname(Long userId, String newNickname) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
    user.setUsername(newNickname);
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

  // 현재 로그인한 유저 가져옴
  public User getCurrentUser() {
    String uuidString =
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    UUID userUuid = UUID.fromString(uuidString);
    return userRepository
        .findByUuid(userUuid)
        .orElseThrow(() -> new IllegalArgumentException("현재 로그인한 사용자를 찾을 수 없습니다."));
  }
}
