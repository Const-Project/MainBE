package com.example.cp_main_be.image.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cp_main_be.util.ApiResponse;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class ImageProcessingServiceTest {

  @Autowired private ImageProcessingService imageProcessingService;

  private static MockWebServer mockWebServer;

  @BeforeAll
  static void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
  }

  @AfterAll
  static void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    // WebClient 테스트를 위한 가짜 API 서버 주소
    registry.add("replicate.api.url", () -> mockWebServer.url("").toString());
    registry.add("replicate.api.token", () -> "test-token");

    // DataSource 빈 생성을 위한 임시 H2 DB 정보
    // 이 테스트는 DB를 직접 쓰진 않지만, @SpringBootTest가 DB 연결을 요구하므로 설정해줍니다.
    registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb");
    registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
    registry.add("spring.datasource.username", () -> "sa");
    registry.add("spring.datasource.password", () -> "");
  }

  @Test
  @DisplayName("AI 서버와의 통신이 모두 성공하면, 성공 ApiResponse를 반환한다")
  void processImageWithAi_success() throws Exception {
    // given (준비)
    String initialResponseJson =
        "{\"status\":\"starting\",\"urls\":{\"get\":\""
            + mockWebServer.url("/predictions/123")
            + "\"}}";
    String statusResponseJson =
        "{\"status\":\"succeeded\",\"output\":[\""
            + mockWebServer.url("/images/result.png")
            + "\"]}";
    byte[] finalImageBytes = "final-image".getBytes();

    mockWebServer.enqueue(
        new MockResponse()
            .setBody(initialResponseJson)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    mockWebServer.enqueue(
        new MockResponse()
            .setBody(statusResponseJson)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    mockWebServer.enqueue(
        new MockResponse()
            .setBody(new okio.Buffer().write(finalImageBytes))
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE));

    MockMultipartFile mockImageFile =
        new MockMultipartFile("image", "test.png", "image/png", new byte[0]);

    // when (실행)
    // 이제 서비스는 동적으로 주입된 MockWebServer URL로 요청을 보낸다.
    ApiResponse<byte[]> response = imageProcessingService.processImageWithAi(mockImageFile);

    // then (검증)
    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getData()).isEqualTo(finalImageBytes);
  }
}
