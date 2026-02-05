package com.example.cp_main_be.domain.avatar.avatar.domain.repository;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {

  @Query(
      value = "SELECT * FROM avatar WHERE is_default_avatar = false ORDER BY RANDOM() LIMIT 1",
      nativeQuery = true)
  Avatar findRandomAvatar();
}
