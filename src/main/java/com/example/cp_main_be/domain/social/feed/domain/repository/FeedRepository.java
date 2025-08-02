package com.example.cp_main_be.domain.social.feed.domain.repository;

import com.example.cp_main_be.domain.social.feed.domain.Feed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepository extends JpaRepository<Feed, Long> {}
