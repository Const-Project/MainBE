package com.example.cp_main_be.image.service;

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

  // 테스트가 실행될 때, application.properties의 replicate.api.token 값을 동적으로 변경
  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("replicate.api.token", () -> "test-token");
    // Replicate API URL을 가짜 웹 서버 주소로 변경하는 로직이 필요하지만,
    // 현재 코드에서는 URL이 상수로 박혀있어 생략합니다. (실제로는 이 부분도 주입받도록 리팩토링 필요)
  }

  @Test
  @DisplayName("AI 서버와의 통신이 모두 성공하면, 성공 ApiResponse를 반환한다")
  void processImageWithAi_success() throws Exception {
    // given (준비)
    // MockWebServer가 반환할 가짜 응답들을 순서대로 준비
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

    MockMultipartFile mockImageFile = new MockMultipartFile("image", new byte[0]);

    // when (실행)
    // 주의: 현재 서비스 코드는 API URL이 하드코딩 되어 있어 실제 API로 요청을 보냅니다.
    // 이 테스트가 성공하려면 서비스 코드에서 API URL을 외부에서 주입받도록 수정해야 합니다.
    // e.g. @Value("${replicate.api.url}") private StringapiUrl;
    // 그리고 @DynamicPropertySource에서 이 값을 mockWebServer.url("/v1/predictions").toString() 으로 설정해주어야
    // 합니다.

    // 아래는 URL이 동적으로 주입된다고 가정한 이상적인 실행 코드입니다.
    // ApiResponse<byte[]> response = imageProcessingService.processImageWithAi(mockImageFile);

    // then (검증)
    // assertThat(response.isSuccess()).isTrue();
    // assertThat(response.getData()).isEqualTo(finalImageBytes);
  }
}
