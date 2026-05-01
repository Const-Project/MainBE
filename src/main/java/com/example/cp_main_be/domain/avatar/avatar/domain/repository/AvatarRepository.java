package com.example.cp_main_be.domain.avatar.avatar.domain.repository;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import com.example.cp_main_be.domain.member.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {

  @Query(value = "SELECT * FROM avatar ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
  Avatar findRandomAvatar();

  @Modifying
  @Query("DELETE FROM Avatar a WHERE a.user = :user")
  void deleteAllByUser(@Param("user") User user);
}
