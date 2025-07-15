package com.example.cp_main_be.user.service;

import com.example.cp_main_be.global.exception.UserNotFoundException;
import com.example.cp_main_be.user.domain.User;
import com.example.cp_main_be.user.domain.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  private final UserRepository userRepository;

  public void saveUser(User user) {
    this.userRepository.save(user);
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

  // 실험용
  public void deleteUser(Long userId) {
    this.userRepository.deleteById(userId);
  }
}
