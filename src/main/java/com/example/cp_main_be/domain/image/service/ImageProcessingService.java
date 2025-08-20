package com.example.cp_main_be.domain.image.service;

import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageProcessingService {

  private final WebClient webClient;

  @Value("${fastapi.server.url}")
  private String fastapiServerUrl;

  /**
   * 이미지를 AI 서버로 보내 아바타를 생성합니다.
   *
   * @param imageFile 원본 이미지 파일
   * @return 생성된 아바타 이미지 byte 배열
   * @throws CustomApiException AI 서버 통신 실패 또는 처리 실패 시 발생
   */
  public byte[] processImageWithAi(MultipartFile imageFile) {
    try {
      // WebClient를 사용하여 FastAPI 서버로 이미지 파일을 POST 요청
      byte[] result =
          webClient
              .post()
              .uri(fastapiServerUrl + "/process-image") // FastAPI의 엔드포인트
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .body(BodyInserters.fromMultipartData("image", imageFile.getResource()))
              .retrieve() // 응답을 받기 시작
              // FastAPI 서버가 4xx, 5xx 에러를 반환하는 경우에 대한 처리
              .onStatus(
                  HttpStatusCode::isError, // HTTP 상태 코드가 에러(4xx, 5xx)인지 확인
                  clientResponse ->
                      clientResponse
                          .bodyToMono(String.class) // 에러 응답 본문을 문자열로 읽어옴 (로깅 목적)
                          .flatMap(
                              errorBody -> {
                                log.error(
                                    "FastAPI server error. Status: {}, Body: {}",
                                    clientResponse.statusCode(),
                                    errorBody);
                                // 정해둔 커스텀 예외를 발생시킴
                                return Mono.error(
                                    new CustomApiException(ErrorCode.AI_AVATAR_FAILED));
                              }))
              .bodyToMono(byte[].class)
              .timeout(Duration.ofSeconds(30)) // 정상 응답(2xx)의 경우, 본문을 byte[]로 변환
              .block(Duration.ofSeconds(30)); // 30초 타임아웃 설정 // 비동기 스트림의 결과를 동기적으로 기다려서 받음
      if (result == null || result.length == 0) {
        log.error("AI 서버에서 유효한 이미지 바이트를 받지 못했습니다 (null/empty).");
        throw new CustomApiException(ErrorCode.AI_AVATAR_FAILED);
      }
      return result;
    } catch (WebClientException e) {
      // 네트워크 연결 실패 등 WebClient 자체의 예외 처리
      log.error("WebClient request failed", e);
      throw new CustomApiException(ErrorCode.AI_AVATAR_FAILED);
    } catch (RuntimeException e) {
      log.error("이미지 생성이 timeout 혹은 예상하지 못한 런타임에러로 인해 실패하였습니다.", e);
      throw new CustomApiException(ErrorCode.AI_AVATAR_FAILED);
    }
  }
}
