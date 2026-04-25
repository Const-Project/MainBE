package com.example.cp_main_be.domain.social.follow.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.follow.domain.Follow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, Long> {
  Optional<Follow> findByFollowerAndFollowing(User follower, User following);

  List<Follow> findByFollower(User follower);

  @Query(
      "SELECT f FROM Follow f "
          + "JOIN FETCH f.following u "
          + "LEFT JOIN FETCH u.avatarList "
          + "WHERE f.follower = :follower")
  List<Follow> findByFollowerWithFollowingAndAvatars(@Param("follower") User follower);

  List<Follow> findByFollowing(User following);

  @Query(
      "SELECT f FROM Follow f "
          + "JOIN FETCH f.follower u "
          + "LEFT JOIN FETCH u.avatarList "
          + "WHERE f.following = :following")
  List<Follow> findByFollowingWithFollowerAndAvatars(@Param("following") User following);

  boolean existsByFollowerAndFollowing(User follower, User following);

  void deleteByFollowerAndFollowing(User follower, User following);

  void deleteAllByFollower(User follower);

  void deleteAllByFollowing(User following);
}
