package com.example.cp_main_be.domain.social.feed.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class RandomFeedSessionStoreConfig {

  private static final Logger log = LoggerFactory.getLogger(RandomFeedSessionStoreConfig.class);

  @Bean
  @ConditionalOnMissingBean(RandomFeedSessionStore.class)
  public RandomFeedSessionStore randomFeedSessionStore(Environment environment) {
    String store = environment.getProperty("random.feed.session.store");
    log.warn(
        "RandomFeedSessionStore bean not found. Falling back to in-memory store. property={}",
        store);
    return new RandomFeedSessionRepository();
  }
}
