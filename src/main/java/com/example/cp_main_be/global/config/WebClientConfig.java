package com.example.cp_main_be.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient webClient() {
    // 메모리 버퍼 사이즈를 늘리기 위한 ExchangeStrategies 설정
    final int MAX_IN_MEMORY_SIZE = 10 * 1024 * 1024; // 10MB로 설정

    ExchangeStrategies exchangeStrategies =
        ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
            .build();

    return WebClient.builder().exchangeStrategies(exchangeStrategies).build();
  }
}
