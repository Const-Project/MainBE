package com.example.cp_main_be.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 클라이언트에게 반환될 에러 응답을 위한 DTO (Data Transfer Object) 이 클래스는 추상 클래스가 아니며, new ErrorResponse(...)를 통해
 * 바로 생성할 수 있습니다.
 */
@Getter
@RequiredArgsConstructor
public class ErrorResponse {

  private final String code; // 우리가 정의한 에러 코드 (예: "E-40401")
  private final String message; // 에러 메시지
}
