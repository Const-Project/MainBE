package com.example.cp_main_be.domain.image.service;

import com.example.cp_main_be.domain.content.image.service.ImageProcessingService;
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

  // FastAPI 서버 주소를 동적으로 설정하도록 변경
  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("fastapi.server.url", () -> mockWebServer.url("/").toString());
    registry.add("jwt.secret", () -> "this-is-a-dummy-secret-key-for-testing-purpose");
    registry.add("r");
  }

  @Test
  @DisplayName("FastAPI 서버에 이미지 처리 요청 성공 시, 성공 ApiResponse를 반환한다")
  void processImageWithAi_fastApi_success() throws Exception {
    // given (준비)
    // FastAPI 서버가 반환할 가짜 이미지 데이터를 준비합니다.
    byte[] processedImageBytes = "processed-image-from-fastapi".getBytes();

    // MockWebServer에 예상되는 응답을 하나만 추가합니다.
    mockWebServer.enqueue(
        new MockResponse()
            .setBody(new okio.Buffer().write(processedImageBytes))
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
            .setResponseCode(200));

    // 서비스에 전달할 Mock 이미지 파일
    MockMultipartFile mockImageFile =
        new MockMultipartFile("image", "test.png", "image/png", "original-image".getBytes());

    // when (실행)
    // ImageProcessingService는 이제 동적으로 주입된 MockWebServer URL로 요청을 보냅니다.
    byte[] response = imageProcessingService.processImageWithAi(mockImageFile);
  }
}
