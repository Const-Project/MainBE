package com.example.cp_main_be.domain.member.user.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByNickname(String username);

  Optional<User> findByUuid(UUID uuid);

  @Query("SELECT u FROM User u LEFT JOIN FETCH u.gardens WHERE u.id = :userId")
  Optional<User> findByIdWithGardens(@Param("userId") Long userId);

  @Query(
      "SELECT u FROM User u "
          + "LEFT JOIN FETCH u.gardens g "
          + "LEFT JOIN FETCH g.avatar a "
          + "LEFT JOIN FETCH a.avatarMaster am "
          + "WHERE u.id = :userId")
  Optional<User> findByIdWithGardensAndAvatars(@Param("userId") Long userId);

  Boolean existsByNickname(String nickname);
}
