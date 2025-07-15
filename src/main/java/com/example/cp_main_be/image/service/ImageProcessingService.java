package com.example.cp_main_be.image.service;

import com.example.cp_main_be.image.dto.ReplicateInitialResponse;
import com.example.cp_main_be.image.dto.ReplicateStatusResponse;
import com.example.cp_main_be.util.ApiResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class ImageProcessingService {

  private final WebClient webClient;

  // private final R2UploadService r2UploadService; // R2 업로드 서비스가 있다고 가정

  @Value("${replicate.api.token}")
  private String replicateApiToken;

  @Value("${replicate.api.url}")
  private String replicateApiBaseUrl;

  // 반환 타입을 ApiResponse<byte[]> 로 변경
  public ApiResponse<byte[]> processImageWithAi(MultipartFile imageFile) {
    try {
      // 1. 이미지를 R2에 업로드하고 공개 URL을 받음 (이 로직은 별도 구현 필요)
      // String imageUrl = r2UploadService.uploadAndGetPublicUrl(imageFile);
      String imageUrl = "https://example.com/uploaded_image.png"; // 임시 URL

      // 2. Replicate에 예측 생성 요청
      ReplicateInitialResponse initialResponse = startPrediction(imageUrl);
      if (initialResponse == null) {
        return ApiResponse.failure("REPLICATE_START_FAILED", "Prediction을 시작하지 못했습니다.");
      }

      // 3. 결과가 나올 때까지 폴링
      String resultImageUrl = pollForResult(initialResponse.urls().get());

      // 4. 최종 결과 이미지를 URL에서 다운로드
      byte[] downloadedImage = downloadImage(resultImageUrl);

      // 5. 성공 시, 이미지 데이터를 담아 성공 응답 반환
      return ApiResponse.success(downloadedImage);

    } catch (Exception e) {
      // 6. 중간에 어떤 예외라도 발생하면, 실패 응답 반환
      // 실제로는 로그를 남기는 것이 좋습니다. e.g., log.error("Replicate failed", e);
      return ApiResponse.failure("REPLICATE_ERROR", e.getMessage());
    }
  }

  private ReplicateInitialResponse startPrediction(String imageUrl) {
    Map<String, Object> input = Map.of("image", imageUrl);
    Map<String, Object> body =
        Map.of(
            "version",
            "cd521c43f75350a3d7639f7b19323381a3498555e76a3e2a86c6158def0373fe", // rembg 모델의 버전 해시
            "input",
            input);

    return webClient
        .post()
        .uri(replicateApiBaseUrl + "/v1/predictions")
        .header("Authorization", "Token " + replicateApiToken)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(ReplicateInitialResponse.class)
        .block();
  }

  private String pollForResult(String statusUrl) {
    while (true) {
      ReplicateStatusResponse statusResponse =
          webClient
              .get()
              .uri(statusUrl)
              .header("Authorization", "Token " + replicateApiToken)
              .retrieve()
              .bodyToMono(ReplicateStatusResponse.class)
              .block();

      if ("succeeded".equals(statusResponse.status())) {
        // 결과는 보통 리스트나 단일 URL로 옵니다. 모델의 출력 형태를 확인해야 합니다.
        return statusResponse.output().get(0);
      } else if ("failed".equals(statusResponse.status())
          || "canceled".equals(statusResponse.status())) {
        throw new RuntimeException("Replicate prediction failed: " + statusResponse.error());
      }

      try {
        // 1초 대기 후 다시 확인
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Polling interrupted", e);
      }
    }
  }

  private byte[] downloadImage(String imageUrl) {
    return webClient.get().uri(imageUrl).retrieve().bodyToMono(byte[].class).block();
  }
}
