package com.example.cp_main_be.domain.social.feed.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.feed.domain.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedRepository extends JpaRepository<Feed, Long> {

  @Modifying
  @Query("DELETE FROM Feed f WHERE f.user = :user")
  void deleteAllByUser(@Param("user") User user);
}
