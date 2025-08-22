package com.example.cp_main_be.domain.social.like.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.like.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
  Optional<Like> findByUserAndTargetIdAndTargetType(User user, Long targetId, String targetType);

  boolean existsByUserAndTargetIdAndTargetType(User user, Long targetId, String targetType);

  //  long countByAvatarPost(AvatarPost post);

  //  Optional<Like> findByUserAndAvatarPost(User currentUser, AvatarPost post);
}
