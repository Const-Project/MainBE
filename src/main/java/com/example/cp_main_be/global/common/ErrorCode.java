package com.example.cp_main_be.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // ... 다른 에러 코드들 ...
  AI_AVATAR_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E-50002", "아바타 생성에 실패했습니다."),
  INVALID_FILE(HttpStatus.BAD_REQUEST, "E-50003", "적절하지 않은 파일 내용/포맷입니다."),
  FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "E-50004", "파일 크기 제한을 넘었습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
