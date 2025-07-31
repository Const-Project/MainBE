package com.example.cp_main_be.domain.social.like.domain.repository;

import com.example.cp_main_be.domain.social.like.domain.Like;
import com.example.cp_main_be.domain.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
  Optional<Like> findByUserAndTargetIdAndTargetType(User user, Long targetId, String targetType);

  boolean existsByUserAndTargetIdAndTargetType(User user, Long targetId, String targetType);
}
