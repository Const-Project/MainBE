package com.example.cp_main_be.domain.member.user.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByNickname(String username);

  Optional<User> findByUuid(UUID uuid);

  Boolean existsByNickname(String nickname);
}
