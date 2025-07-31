package com.example.cp_main_be.domain.image.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cp_main_be.global.util.ApiResponse;
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
import org.springframework.test.context.TestPropertySource; // TestPropertySource 추가

@SpringBootTest
@TestPropertySource(
    properties = {
      "replicate.api.token=test-token",
      "spring.datasource.url=jdbc:h2:mem:testdb",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password="
    }) // 환경 변수 설정
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

  // DynamicPropertySource 제거

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
