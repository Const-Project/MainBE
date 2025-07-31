package com.example.cp_main_be.domain.social.follow.domain.repository;

import com.example.cp_main_be.domain.social.follow.domain.Follow;
import com.example.cp_main_be.domain.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
  Optional<Follow> findByFollowerAndFollowing(User follower, User following);

  List<Follow> findByFollower(User follower);

  List<Follow> findByFollowing(User following);

  boolean existsByFollowerAndFollowing(User follower, User following);
}
