package com.example.cp_main_be.domain.social.avatarpost.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
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
      value = "SELECT ap.id FROM avatar_post ap WHERE ap.id != :excludePostId ORDER BY RAND()",
      nativeQuery = true)
  List<Long> findRandomPublicAvatarPostIds(
      @Param("excludePostId") Long excludePostId, Pageable pageable);

  // [추가] ID 목록으로 AvatarPost 엔티티를 한 번에 조회하는 메서드
  List<AvatarPost> findAllByIdIn(List<Long> ids);

  List<AvatarPost> findAllByUser_IdNotIn(List<Long> blockedUserIds, Pageable pageable);
}
