package com.example.cp_main_be.domain.social.feed.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisRandomFeedSessionStoreTest {

  @Mock private StringRedisTemplate redisTemplate;
  @Mock private ValueOperations<String, String> valueOps;

  private ObjectMapper objectMapper;
  private RedisRandomFeedSessionStore store;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    when(redisTemplate.opsForValue()).thenReturn(valueOps);
    store = new RedisRandomFeedSessionStore(redisTemplate, objectMapper);
  }

  @DisplayName("Redis 세션 저장: TTL 포함 set 호출")
  @Test
  void save_setsValueWithTtl() {
    String token = "token-1";
    RandomFeedSession session =
        new RandomFeedSession(List.of(1L, 2L), List.of(10L), 0, 0, Instant.now().plusSeconds(300));

    store.save(token, session);

    verify(valueOps)
        .set(eq("random_feed_session:" + token), any(String.class), any(Duration.class));
  }

  @DisplayName("Redis 세션 조회: JSON 역직렬화")
  @Test
  void find_deserializesSession() throws Exception {
    String token = "token-2";
    RandomFeedSession session =
        new RandomFeedSession(
            List.of(3L, 4L), List.of(20L, 21L), 1, 2, Instant.now().plusSeconds(600));
    String json = objectMapper.writeValueAsString(session);

    when(valueOps.get("random_feed_session:" + token)).thenReturn(json);

    Optional<RandomFeedSession> result = store.find(token);

    assertTrue(result.isPresent());
    RandomFeedSession loaded = result.get();
    assertEquals(session.getDiaryIds(), loaded.getDiaryIds());
    assertEquals(session.getAvatarPostIds(), loaded.getAvatarPostIds());
    assertEquals(session.getDiaryCursor(), loaded.getDiaryCursor());
    assertEquals(session.getAvatarPostCursor(), loaded.getAvatarPostCursor());
    assertNotNull(loaded.getExpiresAt());
  }

  @DisplayName("Redis 세션 삭제: delete 호출")
  @Test
  void delete_removesKey() {
    String token = "token-3";

    store.delete(token);

    verify(redisTemplate).delete("random_feed_session:" + token);
  }
}
