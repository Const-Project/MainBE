package com.example.cp_main_be.domain.social.feed.session;

import java.util.Optional;

public interface RandomFeedSessionStore {
  void save(String token, RandomFeedSession session);

  Optional<RandomFeedSession> find(String token);

  void delete(String token);
}
