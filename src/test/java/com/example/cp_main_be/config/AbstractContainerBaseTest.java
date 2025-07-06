package com.example.cp_main_be.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers // 1. 이 클래스가 Testcontainers를 사용함을 명시
public abstract class AbstractContainerBaseTest {

  // 2. static으로 컨테이너를 정의합니다. (테스트 전체에 단 하나만 생성)
  private static final PostgreSQLContainer<?> postgresqlContainer =
      new PostgreSQLContainer<>("postgres:15-alpine") // 사용할 PostgreSQL 이미지
          .withDatabaseName("testdb") // 사용할 데이터베이스 이름
          .withUsername("testuser") // 사용자 이름
          .withPassword("testpass"); // 비밀번호

  // 3. 컨테이너를 static 블록에서 수동으로 시작합니다. (JUnit 5 확장기능의 자동시작보다 안정적)
  static {
    postgresqlContainer.start();
  }

  // 4. Spring 컨텍스트가 로드되기 전에, 동적으로 데이터베이스 연결 정보를 설정합니다.
  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgresqlContainer::getUsername);
    registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    registry.add("spring.datasource.driver-class-name", postgresqlContainer::getDriverClassName);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop"); // 테스트가 끝나면 스키마를 삭제하도록 설정
  }
}
