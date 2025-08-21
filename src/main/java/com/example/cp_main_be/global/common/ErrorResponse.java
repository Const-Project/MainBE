package com.example.cp_main_be.global.common;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 클라이언트에게 반환될 에러 응답을 위한 DTO (Data Transfer Object) 이 클래스는 추상 클래스가 아니며, new ErrorResponse(...)를 통해
 * 바로 생성할 수 있습니다.
 */
@Getter
@Builder
public class ErrorResponse {
  private int status;
  private String code;
  private String message;

  public static ErrorResponse of(HttpStatus status, String message) {
    return ErrorResponse.builder()
        .status(status.value())
        .code(status.name())
        .message(message)
        .build();
  }
}
