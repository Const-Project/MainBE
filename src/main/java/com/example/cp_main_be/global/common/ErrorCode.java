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
  INVALID_TOKEN(HttpStatus.BAD_REQUEST, "E-50004", "부적절한 토큰입니다."),
  FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "E-50005", "파일 크기 제한을 넘었습니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "E-50006", "Resource를 찾을 수 없습니다."),
  UPLOAD_FAILED(HttpStatus.EXPECTATION_FAILED, "E-50007", "파일 업로드에 실패했습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
