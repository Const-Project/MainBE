package com.example.cp_main_be.domain.social.diary.domain.Repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
  List<Diary> findByUserInAndIsPublicIsTrue(List<User> users, Pageable pageable);

  List<Diary> findByIsPublicIsTrue(Pageable pageable);

  List<Diary> findByUserOrderByCreatedAtDesc(User user);

  List<Diary> findByUserInAndIsPublicIsTrueAndUser_IdNotIn(
      List<User> users, List<Long> blockedUserIds, Pageable pageable);

  List<Diary> findByIsPublicIsTrueAndUser_IdNotIn(List<Long> blockedUserIds, Pageable pageable);
}
