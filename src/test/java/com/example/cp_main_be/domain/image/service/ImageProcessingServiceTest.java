package com.example.cp_main_be.domain.image.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.cp_main_be.domain.avatar.image.service.ImageProcessingService;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.infra.StorageService;
import java.io.IOException;
import java.util.Base64;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class ImageProcessingServiceTest {

  static MockWebServer mockServer;

  @Mock StorageService storageService;

  ImageProcessingService service;

  @BeforeAll
  static void startServer() throws IOException {
    mockServer = new MockWebServer();
    mockServer.start();
  }

  @AfterAll
  static void stopServer() throws IOException {
    mockServer.shutdown();
  }

  @BeforeEach
  void setUp() {
    WebClient webClient =
        WebClient.builder()
            .exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                    .build())
            .build();

    service = new ImageProcessingService(webClient, storageService);

    String base = mockServer.url("").toString().replaceAll("/$", "");
    ReflectionTestUtils.setField(service, "geminiApiKey", "test-key");
    ReflectionTestUtils.setField(service, "geminiModel", "test-model");
    ReflectionTestUtils.setField(service, "geminiBaseUrl", base);
    ReflectionTestUtils.setField(service, "replicateApiToken", "test-token");
    ReflectionTestUtils.setField(service, "rembgModel", "cjwbw/rembg");
    ReflectionTestUtils.setField(service, "replicateBaseUrl", base);
    ReflectionTestUtils.setField(service, "timeoutSeconds", 10);
    ReflectionTestUtils.setField(service, "validateReference", false);
  }

  private MockMultipartFile pngFile() {
    return new MockMultipartFile("image", "test.png", "image/png", "fake-png-data".getBytes());
  }

  private String geminiResponseWith(byte[] imageBytes) {
    String b64 = Base64.getEncoder().encodeToString(imageBytes);
    return """
        {"candidates":[{"content":{"parts":[{"inlineData":{"mimeType":"image/png","data":"%s"}}]}}]}
        """
        .formatted(b64);
  }

  @Test
  @DisplayName("정상 흐름: Gemini -> Replicate succeeded -> R2 업로드 -> URL 반환")
  void processImageWithAi_success() throws Exception {
    byte[] generatedImage = "generated-image".getBytes();
    byte[] rembgResult = "rembg-result".getBytes();
    String expectedUrl = "https://r2.example.com/avatars/test.png";

    // 1. Gemini 응답
    mockServer.enqueue(
        new MockResponse()
            .setBody(geminiResponseWith(generatedImage))
            .addHeader("Content-Type", "application/json"));

    // 2. Replicate prediction 생성
    mockServer.enqueue(
        new MockResponse()
            .setBody(
                """
                {"id":"pred-123","status":"starting"}
                """)
            .addHeader("Content-Type", "application/json"));

    // 3. Replicate poll - succeeded with output URL
    String outputUrl = mockServer.url("/output-image").toString();
    mockServer.enqueue(
        new MockResponse()
            .setBody(
                """
                {"id":"pred-123","status":"succeeded","output":"%s"}
                """
                    .formatted(outputUrl))
            .addHeader("Content-Type", "application/json"));

    // 4. rembg 결과 이미지 다운로드
    mockServer.enqueue(
        new MockResponse()
            .setBody(new okio.Buffer().write(rembgResult))
            .addHeader("Content-Type", "image/png"));

    given(storageService.uploadFile(any(), eq("avatars/"), any())).willReturn(expectedUrl);

    String result = service.processImageWithAi(pngFile());

    assertThat(result).isEqualTo(expectedUrl);
    verify(storageService).uploadFile(rembgResult, "avatars/", "test.png");
  }

  @Test
  @DisplayName("Gemini 4xx 오류 -> AI_AVATAR_FAILED 예외")
  void processImageWithAi_geminiError_throwsAiAvatarFailed() {
    mockServer.enqueue(new MockResponse().setResponseCode(400).setBody("bad request"));

    assertThatThrownBy(() -> service.processImageWithAi(pngFile()))
        .isInstanceOf(CustomApiException.class)
        .hasMessageContaining(ErrorCode.AI_AVATAR_FAILED.getMessage());
  }

  @Test
  @DisplayName("Gemini 응답에 이미지 part 없음 -> AI_AVATAR_FAILED 예외")
  void processImageWithAi_geminiNoImagePart_throwsAiAvatarFailed() {
    mockServer.enqueue(
        new MockResponse()
            .setBody(
                """
                {"candidates":[{"content":{"parts":[{"text":"sorry, cannot generate"}]}}]}
                """)
            .addHeader("Content-Type", "application/json"));

    assertThatThrownBy(() -> service.processImageWithAi(pngFile()))
        .isInstanceOf(CustomApiException.class)
        .hasMessageContaining(ErrorCode.AI_AVATAR_FAILED.getMessage());
  }

  @Test
  @DisplayName("Replicate prediction failed 상태 -> AI_AVATAR_FAILED 예외")
  void processImageWithAi_replicateFailed_throwsAiAvatarFailed() {
    byte[] generatedImage = "generated-image".getBytes();

    mockServer.enqueue(
        new MockResponse()
            .setBody(geminiResponseWith(generatedImage))
            .addHeader("Content-Type", "application/json"));

    mockServer.enqueue(
        new MockResponse()
            .setBody(
                """
                {"id":"pred-456","status":"starting"}
                """)
            .addHeader("Content-Type", "application/json"));

    mockServer.enqueue(
        new MockResponse()
            .setBody(
                """
                {"id":"pred-456","status":"failed","error":"model error"}
                """)
            .addHeader("Content-Type", "application/json"));

    assertThatThrownBy(() -> service.processImageWithAi(pngFile()))
        .isInstanceOf(CustomApiException.class)
        .hasMessageContaining(ErrorCode.AI_AVATAR_FAILED.getMessage());
  }
}
