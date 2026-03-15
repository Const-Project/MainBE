package com.example.cp_main_be.global.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

public class DatasourceEnvironmentLogger implements EnvironmentPostProcessor, Ordered {

  private static final Logger log = LoggerFactory.getLogger(DatasourceEnvironmentLogger.class);
  private static final String MISSING = "missing";

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    String activeProfiles =
        environment.getActiveProfiles().length == 0
            ? MISSING
            : String.join(",", environment.getActiveProfiles());
    String datasourceUrl = environment.getProperty("spring.datasource.url", MISSING);
    String datasourceUsername = environment.getProperty("spring.datasource.username", MISSING);
    String dbUrl = environment.getProperty("DB_URL", MISSING);
    String dbUsername = environment.getProperty("DB_USERNAME", MISSING);

    // 한글 주석:
    // JPA 초기화 이전 단계에서 실제로 주입된 데이터소스 값을 확인하기 위한 진단 로그다.
    // 비밀번호와 서비스 키는 출력하지 않고, username 만 일부 마스킹해서 남긴다.
    log.info("[startup-datasource] activeProfiles={}", activeProfiles);
    log.info("[startup-datasource] spring.datasource.url={}", datasourceUrl);
    log.info("[startup-datasource] spring.datasource.username={}", mask(datasourceUsername));
    log.info("[startup-datasource] DB_URL={}", dbUrl);
    log.info("[startup-datasource] DB_USERNAME={}", mask(dbUsername));
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  private String mask(String value) {
    if (value == null || value.isBlank() || MISSING.equals(value)) {
      return MISSING;
    }
    if (value.length() <= 8) {
      return "***";
    }
    return value.substring(0, 4) + "***" + value.substring(value.length() - 4);
  }
}
