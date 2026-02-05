package com.example.cp_main_be.domain.social.avatarpost.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvatarPostRepository extends JpaRepository<AvatarPost, Long> {
  List<AvatarPost> findByUserIn(List<User> users, Pageable pageable);

  @Query(
      "SELECT DISTINCT ap FROM AvatarPost ap "
          + "LEFT JOIN FETCH ap.user u " // 포스트 작성자 fetch
          + "LEFT JOIN FETCH ap.comments c " // 댓글 목록 fetch
          + "LEFT JOIN FETCH c.writer cw " // 댓글 작성자 fetch
          + "WHERE ap.id = :postId")
  Optional<AvatarPost> findByIdWithDetails(@Param("postId") Long postId);

  List<AvatarPost> findAllBy(Pageable pageable);

  List<AvatarPost> findByUserInAndUser_IdNotIn(
      List<User> users, List<Long> blockedUserIds, Pageable pageable);

  // [추가] 랜덤으로 아바타 포스트 ID 목록을 조회하는 쿼리 (MySQL 기준)
  // AvatarPost에는 isPublic 필드가 없으므로 모든 포스트를 대상으로 합니다.
  @Query(
      value =
          "SELECT ap.avatar_post_id FROM avatar_post ap WHERE ap.avatar_post_id != :excludePostId ORDER BY RANDOM()",
      nativeQuery = true)
  List<Long> findRandomPublicAvatarPostIds(
      @Param("excludePostId") Long excludePostId, Pageable pageable);

  // [추가] ID 목록으로 AvatarPost 엔티티를 한 번에 조회하는 메서드
  List<AvatarPost> findAllByIdIn(List<Long> ids);

  List<AvatarPost> findAllByUser_IdNotIn(List<Long> blockedUserIds, Pageable pageable);

  // 커서 기반 페이지네이션을 위한 메서드 (전체 피드용)
  // @EntityGraph를 사용하면 더 깔끔하게 N+1 문제 해결 가능
  @EntityGraph(attributePaths = {"user"}) // user 정보를 함께 EAGER 조회
  List<AvatarPost> findByCreatedAtBeforeOrderByCreatedAtDesc(
      LocalDateTime cursor, Pageable pageable);

  @Query(
      "SELECT ap FROM AvatarPost ap JOIN FETCH ap.user WHERE ap.createdAt < :cursor "
          + "AND ("
          + " :#{#blockedUserIds == null} = true"
          + " OR :#{#blockedUserIds.isEmpty()} = true"
          + " OR ap.user.id NOT IN (:blockedUserIds)"
          + ") ORDER BY ap.createdAt DESC")
  List<AvatarPost> findByCreatedAtBeforeAndUserIdNotInOrderByCreatedAtDesc(
      @Param("cursor") LocalDateTime cursor,
      @Param("blockedUserIds") List<Long> blockedUserIds,
      Pageable pageable);

  // 커서 기반 페이지네이션을 위한 메서드 (팔로잉 피드용)
  @EntityGraph(attributePaths = {"user"})
  List<AvatarPost> findByUserInAndCreatedAtBeforeOrderByCreatedAtDesc(
      List<User> followingUsers, LocalDateTime cursor, Pageable pageable);

  @Query(
      "SELECT ap FROM AvatarPost ap JOIN FETCH ap.user WHERE ap.user IN :followingUsers AND ap.createdAt < :cursor "
          + "AND ("
          + " :#{#blockedUserIds == null} = true"
          + " OR :#{#blockedUserIds.isEmpty()} = true"
          + " OR ap.user.id NOT IN (:blockedUserIds)"
          + ") ORDER BY ap.createdAt DESC")
  List<AvatarPost> findByUserInAndUserIdNotInAndCreatedAtBeforeOrderByCreatedAtDesc(
      @Param("followingUsers") List<User> followingUsers,
      @Param("blockedUserIds") List<Long> blockedUserIds,
      @Param("cursor") LocalDateTime cursor,
      Pageable pageable);

  // AvatarPostRepository.java
  @Query(
      value =
          """
          SELECT ap.avatar_post_id
          FROM avatar_post ap
          WHERE (
              :#{#excludeIds == null} = true
              OR :#{#excludeIds.isEmpty()} = true
              OR ap.avatar_post_id NOT IN (:excludeIds)
          )
          ORDER BY RANDOM()
          LIMIT :limit
          """,
      nativeQuery = true)
  List<Long> findRandomPublicAvatarPostIdsExcluding(
      @Param("excludeIds") List<Long> excludeIds, @Param("limit") int limit);

  @Query(
      value =
          """
          SELECT ap.avatar_post_id
          FROM avatar_post ap
          WHERE (
              :#{#excludeIds == null} = true
              OR :#{#excludeIds.isEmpty()} = true
              OR ap.avatar_post_id NOT IN (:excludeIds)
          )
          AND (
              :#{#blockedUserIds == null} = true
              OR :#{#blockedUserIds.isEmpty()} = true
              OR ap.user_id NOT IN (:blockedUserIds)
          )
          ORDER BY RANDOM()
          LIMIT :limit
          """,
      nativeQuery = true)
  List<Long> findRandomPublicAvatarPostIdsExcludingAndBlocked(
      @Param("excludeIds") List<Long> excludeIds,
      @Param("blockedUserIds") List<Long> blockedUserIds,
      @Param("limit") int limit);
}
