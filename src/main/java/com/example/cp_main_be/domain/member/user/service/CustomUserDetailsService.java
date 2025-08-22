package com.example.cp_main_be.domain.member.user.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String uuid) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByUuid(UUID.fromString(uuid))
            .orElseThrow(() -> new UsernameNotFoundException("User not found with uuid: " + uuid));

    // DB에서 조회한 '우리 User 엔티티'를 그대로 반환합니다.
    return user;
  }
}
