package com.example.cp_main_be.user.service;

import com.example.cp_main_be.user.domain.User;
import com.example.cp_main_be.user.domain.repository.UserRepository;
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
    return this.userRepository.findById(id).orElse(null);
  }

  // 실험용
  public void deleteUser(Long userId) {
    this.userRepository.deleteById(userId);
  }
}
