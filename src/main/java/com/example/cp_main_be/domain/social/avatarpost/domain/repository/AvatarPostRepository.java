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

  List<AvatarPost> findByUserInAndUser_IdNotIn(
      List<User> users, List<Long> blockedUserIds, Pageable pageable);

  List<AvatarPost> findAllByUser_IdNotIn(List<Long> blockedUserIds, Pageable pageable);
}
