package com.example.cp_main_be.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers // 1. 이 클래스가 Testcontainers를 사용함을 명시
public abstract class AbstractContainerBaseTest {

  // 2. static으로 컨테이너를 정의합니다. (테스트 전체에 단 하나만 생성)
  @Container
  public static final PostgreSQLContainer<?> postgresqlContainer =
      new PostgreSQLContainer<>("postgres:15-alpine") // 사용할 PostgreSQL 이미지
          .withDatabaseName("testdb") // 사용할 데이터베이스 이름
          .withUsername("testuser") // 사용자 이름
          .withPassword("testpass"); // 비밀번호

  // 4. Spring 컨텍스트가 로드되기 전에, 동적으로 데이터베이스 연결 정보를 설정합니다.
  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    // 데이터베이스 연결 정보 설정
    registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgresqlContainer::getUsername);
    registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    registry.add("spring.datasource.driver-class-name", postgresqlContainer::getDriverClassName);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

    // 3. 누락된 다른 프로퍼티들을 여기에 추가해야 합니다!
    // 이 값들이 없으면 Spring Context가 로드되지 못합니다.
    registry.add("cloudflare.r2.endpoint", () -> "http://localhost:9000"); // 테스트용 더미값
    registry.add("cloudflare.r2.bucket", () -> "test-bucket");
    registry.add("cloudflare.r2.access-key", () -> "test-key");
    registry.add("cloudflare.r2.secret-key", () -> "test-secret");

    registry.add("replicate.api.token", () -> "dummy-token");
    registry.add("replicate.api.url", () -> "https://api.replicate.com/test");
  }
}
