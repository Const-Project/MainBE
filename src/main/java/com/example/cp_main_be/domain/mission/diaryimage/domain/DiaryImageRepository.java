package com.example.cp_main_be.domain.mission.diaryimage.domain;

import com.example.cp_main_be.domain.member.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryImageRepository extends JpaRepository<DiaryImage, Long> {

  @Modifying
  @Query("DELETE FROM DiaryImage di WHERE di.user = :user")
  void deleteAllByUser(@Param("user") User user);
}
