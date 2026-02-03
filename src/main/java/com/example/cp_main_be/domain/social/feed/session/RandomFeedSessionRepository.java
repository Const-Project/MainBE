package com.example.cp_main_be.domain.social.feed.session;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class RandomFeedSessionRepository {
  private final Map<String, RandomFeedSession> sessions = new ConcurrentHashMap<>();
  private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

  public RandomFeedSessionRepository() {
    cleaner.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.MINUTES);
  }

  public void save(String token, RandomFeedSession session) {
    sessions.put(token, session);
  }

  public Optional<RandomFeedSession> find(String token) {
    RandomFeedSession session = sessions.get(token);
    if (session == null) {
      return Optional.empty();
    }
    if (session.getExpiresAt().isBefore(Instant.now())) {
      sessions.remove(token);
      return Optional.empty();
    }
    return Optional.of(session);
  }

  public void delete(String token) {
    sessions.remove(token);
  }

  private void cleanup() {
    Instant now = Instant.now();
    sessions.entrySet().removeIf(entry -> entry.getValue().getExpiresAt().isBefore(now));
  }
}
