package com.example.cp_main_be.domain.social.feed.session;

import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "random.feed.session.store", havingValue = "redis")
public class RedisRandomFeedSessionStore implements RandomFeedSessionStore {

  private static final String KEY_PREFIX = "random_feed_session:";

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void save(String token, RandomFeedSession session) {
    String key = KEY_PREFIX + token;
    try {
      String payload = objectMapper.writeValueAsString(session);
      long ttlSeconds =
          Math.max(
              1, Duration.between(java.time.Instant.now(), session.getExpiresAt()).getSeconds());
      redisTemplate.opsForValue().set(key, payload, Duration.ofSeconds(ttlSeconds));
    } catch (JsonProcessingException e) {
      throw new CustomApiException(ErrorCode.INVALID_REQUEST, "랜덤 피드 세션 직렬화에 실패했습니다.");
    }
  }

  @Override
  public Optional<RandomFeedSession> find(String token) {
    String key = KEY_PREFIX + token;
    String payload = redisTemplate.opsForValue().get(key);
    if (payload == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(objectMapper.readValue(payload, RandomFeedSession.class));
    } catch (JsonProcessingException e) {
      throw new CustomApiException(ErrorCode.INVALID_REQUEST, "랜덤 피드 세션 역직렬화에 실패했습니다.");
    }
  }

  @Override
  public void delete(String token) {
    redisTemplate.delete(KEY_PREFIX + token);
  }
}
