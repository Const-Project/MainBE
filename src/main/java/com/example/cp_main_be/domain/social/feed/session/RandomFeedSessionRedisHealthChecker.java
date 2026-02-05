package com.example.cp_main_be.domain.social.feed.session;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "random.feed.session.store", havingValue = "redis")
@ConditionalOnBean(StringRedisTemplate.class)
public class RandomFeedSessionRedisHealthChecker implements ApplicationRunner {

  private static final Logger log =
      LoggerFactory.getLogger(RandomFeedSessionRedisHealthChecker.class);

  private final StringRedisTemplate redisTemplate;

  @Override
  public void run(ApplicationArguments args) {
    try {
      String pong = redisTemplate.getConnectionFactory().getConnection().ping();
      log.info("RandomFeedSessionStore=REDIS, redis ping={}", pong);
    } catch (Exception e) {
      log.error("RandomFeedSessionStore=REDIS but redis ping failed", e);
    }
  }
}
