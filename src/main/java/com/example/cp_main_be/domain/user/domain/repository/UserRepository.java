package com.example.cp_main_be.domain.user.domain.repository;

import com.example.cp_main_be.domain.user.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  Optional<User> findByUuid(UUID uuid);
}
