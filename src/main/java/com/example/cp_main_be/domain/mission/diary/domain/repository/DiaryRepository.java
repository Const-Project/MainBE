package com.example.cp_main_be.domain.mission.diary.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import com.example.cp_main_be.domain.mission.user_daily_mission.dto.MissionCountPerDay;
import java.time.LocalDateTime;
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

  @Query(
      "SELECT d FROM Diary d "
          + "WHERE d.user = :user AND d.createdAt BETWEEN :startDate AND :endDate")
  List<Diary> findTodayDiaryByUser(
      @Param("user") User user,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT new com.example.cp_main_be.domain.mission.user_daily_mission.dto.MissionCountPerDay(DAY(d.createdAt), COUNT(d.id)) "
          + "FROM Diary d "
          + "WHERE d.user = :user "
          + "  AND d.createdAt BETWEEN :startDate AND :endDate "
          + "GROUP BY DAY(d.createdAt)")
  List<MissionCountPerDay> findCompletedCountsPerDay(
      @Param("user") User user,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT d FROM Diary d "
          + "WHERE d.user = :user "
          + "  AND YEAR(d.createdAt) = :year "
          + "  AND MONTH(d.createdAt) = :month "
          + "ORDER BY d.createdAt DESC")
  List<Diary> findByUserAndYearAndMonth(
      @Param("user") User user, @Param("year") int year, @Param("month") int month);
}
