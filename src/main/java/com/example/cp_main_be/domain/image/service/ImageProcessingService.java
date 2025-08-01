package com.example.cp_main_be.domain.image.service;

import com.example.cp_main_be.global.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class ImageProcessingService {

  private final WebClient webClient;

  // FastAPI 서버 주소는 application.yml 등에서 주입
  @Value("${fastapi.server.url}")
  private String fastapiServerUrl;

  public ApiResponse<byte[]> processImageWithAi(MultipartFile imageFile) {
    try {
      // FastAPI 서버로 직접 이미지 파일을 POST 요청
      byte[] processedImage =
          webClient
              .post()
              .uri(fastapiServerUrl + "/process-image") // FastAPI의 엔드포인트
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .body(BodyInserters.fromMultipartData("image", imageFile.getResource()))
              .retrieve() // 에러 처리를 위해 onStatus 사용 가능
              .bodyToMono(byte[].class)
              // .block(); // 실제로는 block() 대신 아래와 같이 비동기 체이닝을 사용해야 합니다.
              .flux() // Mono를 Flux로 변환
              .toStream() // Flux를 스트림으로 변환
              .findFirst() // 첫 번째 요소(결과)를 Optional로 가져옴
              .orElse(null); // Optional이 비어있으면 null 반환

      if (processedImage == null) {
        return ApiResponse.failure("FASTAPI_ERROR", "FastAPI 서버 처리 중 오류 발생");
      }

      return ApiResponse.success(processedImage);

    } catch (Exception e) {
      return ApiResponse.failure("FASTAPI_ERROR", e.getMessage());
    }
  }
}
