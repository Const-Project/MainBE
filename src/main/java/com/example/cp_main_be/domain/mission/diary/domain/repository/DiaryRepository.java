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

  boolean existsByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);

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

  @Query(
      value =
          "SELECT d.diary_id FROM diaries d WHERE d.is_public = true "
              + "AND (:excludePostId IS NULL OR d.diary_id <> :excludePostId) ORDER BY RAND()",
      nativeQuery = true)
  List<Long> findRandomPublicDiaryIds(
      @Param("excludePostId") Long excludePostId, Pageable pageable);

  // [추가] ID 목록으로 Diary 엔티티를 한 번에 조회하는 메서드
  List<Diary> findAllByIdIn(List<Long> ids);

  // 커서 기반 페이지네이션을 위한 메서드 (전체 피드용)
  // user와 comments를 함께 조회하여 N+1 문제 해결
  @Query(
      "SELECT d FROM Diary d JOIN FETCH d.user WHERE d.isPublic = true AND d.createdAt < :cursor ORDER BY d.createdAt DESC")
  List<Diary> findPublicDiariesWithCursor(@Param("cursor") LocalDateTime cursor, Pageable pageable);

  @Query(
      "SELECT d FROM Diary d JOIN FETCH d.user WHERE d.isPublic = true AND d.createdAt < :cursor "
          + "AND ("
          + " :#{#blockedUserIds == null} = true"
          + " OR :#{#blockedUserIds.isEmpty()} = true"
          + " OR d.user.id NOT IN (:blockedUserIds)"
          + ") ORDER BY d.createdAt DESC")
  List<Diary> findPublicDiariesWithCursorExcludingBlocked(
      @Param("cursor") LocalDateTime cursor,
      @Param("blockedUserIds") List<Long> blockedUserIds,
      Pageable pageable);

  // 커서 기반 페이지네이션을 위한 메서드 (팔로잉 피드용)
  @Query(
      "SELECT d FROM Diary d JOIN FETCH d.user WHERE d.user IN :followingUsers AND d.isPublic = true AND d.createdAt < :cursor ORDER BY d.createdAt DESC")
  List<Diary> findFollowingDiariesWithCursor(
      @Param("followingUsers") List<User> followingUsers,
      @Param("cursor") LocalDateTime cursor,
      Pageable pageable);

  @Query(
      "SELECT d FROM Diary d JOIN FETCH d.user WHERE d.user IN :followingUsers AND d.isPublic = true AND d.createdAt < :cursor "
          + "AND ("
          + " :#{#blockedUserIds == null} = true"
          + " OR :#{#blockedUserIds.isEmpty()} = true"
          + " OR d.user.id NOT IN (:blockedUserIds)"
          + ") ORDER BY d.createdAt DESC")
  List<Diary> findFollowingDiariesWithCursorExcludingBlocked(
      @Param("followingUsers") List<User> followingUsers,
      @Param("cursor") LocalDateTime cursor,
      @Param("blockedUserIds") List<Long> blockedUserIds,
      Pageable pageable);

  // ⭐⭐ 새로 추가: 리스트 제외용 메서드
  // SpEL로 빈 리스트 체크 (가장 안전한 방법)
  @Query(
      value =
          """
          SELECT d.diary_id
          FROM diaries d
          WHERE d.is_public = true
          AND (
              :#{#excludeIds == null} = true
              OR :#{#excludeIds.isEmpty()} = true
              OR d.diary_id NOT IN (:excludeIds)
          )
          ORDER BY RAND()
          LIMIT :limit
          """,
      nativeQuery = true)
  List<Long> findRandomPublicDiaryIdsExcluding(
      @Param("excludeIds") List<Long> excludeIds, @Param("limit") int limit);

  @Query(
      value =
          """
          SELECT d.diary_id
          FROM diaries d
          WHERE d.is_public = true
          AND (
              :#{#excludeIds == null} = true
              OR :#{#excludeIds.isEmpty()} = true
              OR d.diary_id NOT IN (:excludeIds)
          )
          AND (
              :#{#blockedUserIds == null} = true
              OR :#{#blockedUserIds.isEmpty()} = true
              OR d.user_id NOT IN (:blockedUserIds)
          )
          ORDER BY RAND()
          LIMIT :limit
          """,
      nativeQuery = true)
  List<Long> findRandomPublicDiaryIdsExcludingAndBlocked(
      @Param("excludeIds") List<Long> excludeIds,
      @Param("blockedUserIds") List<Long> blockedUserIds,
      @Param("limit") int limit);
}
