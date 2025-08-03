package com.example.cp_main_be.domain.user.service;

import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import java.util.ArrayList;
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

    return new org.springframework.security.core.userdetails.User(
        user.getUuid().toString(),
        "", // 비밀번호는 사용하지 않으므로 빈 문자열
        new ArrayList<>() // 권한은 현재 없으므로 빈 리스트
        );
  }
}
