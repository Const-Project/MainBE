package com.example.cp_main_be.domain.avatar.image.service;

import com.example.cp_main_be.domain.avatar.image.AiResponseDTO;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.infra.S3Uploader;
import com.example.cp_main_be.global.infra.StorageService;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ImageProcessingService {

  private static final Logger log = LoggerFactory.getLogger(ImageProcessingService.class);

  private final WebClient webClient;
  private final StorageService storageService;
  private final S3Uploader s3Uploader;

  @Value("${fastapi.server.url}")
  private String fastapiServerUrl;

  private static final Random random = new Random();

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
   * 이미지를 바이너리 데이터로 AI 서버에 전송하여 아바타를 생성하고, 결과 이미지의 URL을 반환합니다.
   *
   * @param imageFile 원본 이미지 파일
   * @return 생성된 아바타 이미지의 최종 URL
   * @throws CustomApiException AI 서버 통신 실패 시 발생 (실패 시 폴백 URL 반환)
   */
  public String processImageWithAi(MultipartFile imageFile) {
    try {
      MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
      bodyBuilder.part("image", new ByteArrayResource(imageFile.getBytes()))
              .filename(imageFile.getOriginalFilename()) // 원래 파일명 사용
              .contentType(MediaType.parseMediaType(imageFile.getContentType())); // 원본 Content-Type 유지

// WebClient 요청 시
      // 2. WebClient를 사용하여 multipart/form-data 형식으로 전송
      AiResponseDTO responseDTO =
          webClient
              .post()
              .uri(fastapiServerUrl + "/process-image")
              .contentType(MediaType.MULTIPART_FORM_DATA) // multipart/form-data로 설정
                  .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
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
//                  .bodyToMono(byte[].class)
              .bodyToMono(AiResponseDTO.class)
              .timeout(Duration.ofSeconds(300))
              .block(Duration.ofSeconds(300));

      byte[] result = Base64.getDecoder().decode(responseDTO.getImageData());

      if (result == null || result.length == 0) {
        log.error("AI 서버에서 유효한 이미지 링크를 받지 못했습니다 (null/empty).");
        throw new CustomApiException(ErrorCode.AI_AVATAR_FAILED);
      }

//       3. 받은 byte 배열을 스토리지에 업로드하고 URL을 받음
      String imageUrl = s3Uploader.uploadByteArrayToPng(result,"/avatars");

      return imageUrl;

    } catch (Exception e) {
      // 실패 시 폴백 URL 리스트에서 랜덤으로 하나를 선택하여 반환
      log.warn("AI 아바타 생성 실패. 폴백 이미지 URL을 반환합니다. 원인: {}", e.getMessage());
      String fallbackUrl = FALLBACK_IMAGE_URLS.get(random.nextInt(FALLBACK_IMAGE_URLS.size()));
      log.info("선택된 폴백 이미지 URL: {}", fallbackUrl);
      return fallbackUrl;
    }
  }

  /** 대안: 특정 이미지 포맷으로 Content-Type을 설정하여 전송 */
  public String processImageWithAiWithSpecificContentType(MultipartFile imageFile) {
    try {
      byte[] imageBytes = imageFile.getBytes();

      // 파일 확장자에 따라 Content-Type 결정
      String contentType = determineContentType(imageFile.getOriginalFilename());

      byte[] result =
          webClient
              .post()
              .uri(fastapiServerUrl + "/process-image")
              .contentType(MediaType.parseMediaType(contentType)) // 이미지 타입에 맞는 Content-Type
              .body(BodyInserters.fromValue(imageBytes))
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
              .timeout(Duration.ofSeconds(300))
              .block(Duration.ofSeconds(300));

      if (result == null || result.length == 0) {
        log.error("AI 서버에서 유효한 이미지 바이트를 받지 못했습니다 (null/empty).");
        throw new CustomApiException(ErrorCode.AI_AVATAR_FAILED);
      }

      String imageUrl =
          storageService.uploadFile(result, "avatars/", imageFile.getOriginalFilename());

      return imageUrl;

    } catch (Exception e) {
      log.warn("AI 아바타 생성 실패. 폴백 이미지 URL을 반환합니다. 원인: {}", e.getMessage());
      String fallbackUrl = FALLBACK_IMAGE_URLS.get(random.nextInt(FALLBACK_IMAGE_URLS.size()));
      log.info("선택된 폴백 이미지 URL: {}", fallbackUrl);
      return fallbackUrl;
    }
  }

  /** 파일 확장자에 따라 적절한 Content-Type을 반환 */
  private String determineContentType(String filename) {
    if (filename == null) {
      return "application/octet-stream";
    }

    String extension = filename.toLowerCase();
    if (extension.endsWith(".png")) {
      return "image/png";
    } else if (extension.endsWith(".jpg") || extension.endsWith(".jpeg")) {
      return "image/jpeg";
    } else if (extension.endsWith(".gif")) {
      return "image/gif";
    } else if (extension.endsWith(".webp")) {
      return "image/webp";
    } else {
      return "application/octet-stream";
    }
  }

  public String getDefaultImageUrl() {
    return FALLBACK_IMAGE_URLS.get(random.nextInt(FALLBACK_IMAGE_URLS.size()));
  }
}
