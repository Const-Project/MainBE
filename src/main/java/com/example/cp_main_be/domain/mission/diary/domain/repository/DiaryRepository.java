package com.example.cp_main_be.domain.mission.diary.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
  List<Diary> findByUserInAndIsPublicIsTrue(List<User> users, Pageable pageable);

  List<Diary> findByIsPublicIsTrue(Pageable pageable);

  List<Diary> findByUserOrderByCreatedAtDesc(User user);

  List<Diary> findByUserInAndIsPublicIsTrueAndUser_IdNotIn(
      List<User> users, List<Long> blockedUserIds, Pageable pageable);

  List<Diary> findByIsPublicIsTrueAndUser_IdNotIn(List<Long> blockedUserIds, Pageable pageable);

  @Query(
      "SELECT DISTINCT d FROM Diary d "
          + "LEFT JOIN FETCH d.user u " // 포스트 작성자 fetch
          + "LEFT JOIN FETCH d.diaryImage di " // 이미지 fetch
          + "LEFT JOIN FETCH d.comments c " // 댓글 목록 fetch
          + "LEFT JOIN FETCH c.writer cw " // 댓글 작성자 fetch
          + "WHERE d.id = :diaryId")
  Optional<Diary> findByIdWithDetails(@Param("diaryId") Long diaryId);
}
