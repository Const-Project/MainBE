package com.example.cp_main_be.domain.social.like.avatar_post.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.like.avatar_post.domain.AvatarPostLike;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvatarPostLikeRepository extends JpaRepository<AvatarPostLike, Long> {

  Optional<AvatarPostLike> findByUserAndAvatarPost(User user, AvatarPost avatarPost);

  boolean existsByUserAndAvatarPost(User user, AvatarPost avatarPost);

  long countByAvatarPost(AvatarPost avatarPost);

  void deleteByUserAndAvatarPost(User user, AvatarPost avatarPost);

  void deleteAllByUser(User user);

  @Modifying
  @Query("DELETE FROM AvatarPostLike apl WHERE apl.avatarPost.user = :user")
  void deleteAllByAvatarPostUser(@Param("user") User user);

  @Query(
      "SELECT l.avatarPost.id, COUNT(l.id) FROM AvatarPostLike l WHERE l.avatarPost.id IN :postIds GROUP BY l.avatarPost.id")
  List<Object[]> countByAvatarPostIds(@Param("postIds") List<Long> postIds);

  default Map<Long, Long> countLikesByAvatarPostIds(List<Long> postIds) {
    if (postIds == null || postIds.isEmpty()) {
      return Map.of();
    }
    return countByAvatarPostIds(postIds).stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
  }
}
