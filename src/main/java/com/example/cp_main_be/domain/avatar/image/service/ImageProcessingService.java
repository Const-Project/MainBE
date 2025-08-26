package com.example.cp_main_be.domain.avatar.image.service;

import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.infra.StorageService;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ImageProcessingService {

  private static final Logger log = LoggerFactory.getLogger(ImageProcessingService.class);

  private final WebClient webClient;
  private final StorageService storageService; // [추가] 스토리지 서비스 주입

  @Value("${fastapi.server.url}")
  private String fastapiServerUrl;

  private static final Random random = new Random();

  // [기존과 동일] 실패 시 사용할 이미지 URL 목록
  private static final List<String> FALLBACK_IMAGE_URLS =
      List.of(
          "https://pub-5cb74645f3e1443686dd7aa7096913f1.r2.dev/%E1%84%86%E1%85%A9%E1%86%AB%E1%84%89%E1%85%B3%E1%84%90%E1%85%A6%E1%84%85%E1%85%A1%201.png",
          "https://pub-5cb74645f3e1443686dd7aa7096913f1.r2.dev/%E1%84%87%E1%85%A2%E1%86%A8%E1%84%83%E1%85%A9%E1%84%89%E1%85%A5%E1%86%AB%201.png",
          "https://pub-5cb74645f3e1443686dd7aa7096913f1.r2.dev/%E1%84%87%E1%85%A2%E1%86%BC%E1%84%80%E1%85%A1%E1%86%AF%E1%84%80%E1%85%A9%E1%84%86%E1%85%AE%E1%84%82%E1%85%A1%E1%84%86%E1%85%AE%201.png",
          "https://pub-5cb74645f3e1443686dd7aa7096913f1.r2.dev/%E1%84%89%E1%85%A1%E1%86%AB%E1%84%89%E1%85%A6%E1%84%87%E1%85%A6%E1%84%85%E1%85%B5%E1%84%8B%E1%85%A1%201.png",
          "https://pub-5cb74645f3e1443686dd7aa7096913f1.r2.dev/%E1%84%89%E1%85%A5%E1%84%8B%E1%85%A3%E1%86%BC%E1%84%85%E1%85%A1%E1%86%AB%201.png",
          "https://pub-5cb74645f3e1443686dd7aa7096913f1.r2.dev/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B5%E1%86%AB%E1%84%83%E1%85%A1%E1%86%B8%E1%84%89%E1%85%A5%E1%84%89%E1%85%B3%201.png",
          "https://pub-5cb74645f3e1443686dd7aa7096913f1.r2.dev/%E1%84%91%E1%85%A1%E1%84%8F%E1%85%B5%E1%84%85%E1%85%A1%201.png",
          "https://pub-5cb74645f3e1443686dd7aa7096913f1.r2.dev/%E1%84%92%E1%85%A2%E1%86%BC%E1%84%8B%E1%85%AE%E1%86%AB%E1%84%86%E1%85%A9%E1%86%A8%201.png");

  /**
   * 이미지를 AI 서버로 보내 아바타를 생성하고, 결과 이미지의 URL을 반환합니다.
   *
   * @param imageFile 원본 이미지 파일
   * @return 생성된 아바타 이미지의 최종 URL
   * @throws CustomApiException AI 서버 통신 실패 시 발생 (실패 시 폴백 URL 반환)
   */
  public String processImageWithAi(MultipartFile imageFile) {
    try {
      // 1. WebClient를 사용하여 FastAPI 서버로부터 이미지 byte 배열을 받음
      byte[] result =
          webClient
              .post()
              .uri(fastapiServerUrl + "/process-image")
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .body(BodyInserters.fromMultipartData("image", imageFile.getResource()))
              .retrieve()
              .onStatus(
                  HttpStatusCode::isError,
                  clientResponse ->
                      clientResponse
                          .bodyToMono(String.class)
                          .flatMap(
                              errorBody -> {
                                log.error(
                                    "FastAPI server error. Status: {}, Body: {}",
                                    clientResponse.statusCode(),
                                    errorBody);
                                return Mono.error(
                                    new CustomApiException(ErrorCode.AI_AVATAR_FAILED));
                              }))
              .bodyToMono(byte[].class)
              .timeout(Duration.ofSeconds(30))
              .block(Duration.ofSeconds(30));

      if (result == null || result.length == 0) {
        log.error("AI 서버에서 유효한 이미지 바이트를 받지 못했습니다 (null/empty).");
        throw new CustomApiException(ErrorCode.AI_AVATAR_FAILED);
      }

      // 2. [변경] 성공 시, 받은 byte 배열을 스토리지에 업로드하고 URL을 받음
      // "avatars/"는 스토리지 내 폴더 경로, 두 번째 인자는 파일 이름
      String imageUrl =
          storageService.uploadFile(result, "avatars/", imageFile.getOriginalFilename());

      // 3. [변경] 스토리지로부터 받은 URL을 반환
      return imageUrl;

    } catch (Exception e) {
      // [변경] 실패 시, 폴백 URL 리스트에서 랜덤으로 하나를 선택하여 바로 반환
      log.warn("AI 아바타 생성 실패. 폴백 이미지 URL을 반환합니다. 원인: {}", e.getMessage());
      String fallbackUrl = FALLBACK_IMAGE_URLS.get(random.nextInt(FALLBACK_IMAGE_URLS.size()));
      log.info("선택된 폴백 이미지 URL: {}", fallbackUrl);
      return fallbackUrl;
    }
  }

  // 이 메서드는 이제 사용되지 않지만, 다른 곳에서 사용될 수 있으므로 유지합니다.
  public String getDefaultImageUrl() {
    return FALLBACK_IMAGE_URLS.get(random.nextInt(FALLBACK_IMAGE_URLS.size()));
  }
}
