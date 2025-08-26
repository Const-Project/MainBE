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
  UPLOAD_FAILED(HttpStatus.EXPECTATION_FAILED, "E-50007", "파일 업로드에 실패했습니다."),
  MAX_GARDENS_REACHED(HttpStatus.BAD_REQUEST, "E-50008", "현재 레벨의 최대 정원수에 도달했습니다.");
  // === 4xx Client Errors ===
  // 400 Bad Request
  INVALID_TOKEN(HttpStatus.BAD_REQUEST, "E40001", "부적절한 토큰입니다."),
  WATERING_COOL_DOWN(HttpStatus.BAD_REQUEST, "E40002", "아직 물을 줄 수 없습니다. 8시간이 지나야 가능합니다."),
  FRIEND_WATERING_LIMIT_EXCEEDED(
      HttpStatus.BAD_REQUEST, "E40003", "오늘은 다른 사람의 정원에 더 이상 물을 줄 수 없습니다."),
  ALREADY_WATERED_GARDEN(HttpStatus.BAD_REQUEST, "E40004", "이 정원에는 오늘 이미 물을 주었습니다."),
  SUNLIGHT_COOL_DOWN(HttpStatus.BAD_REQUEST, "E40005", "오늘은 이미 햇빛을 주었습니다."),
  GARDEN_SLOT_MAXED_OUT(HttpStatus.BAD_REQUEST, "E40006", "더 이상 텃밭을 추가할 수 없습니다."),
  INVALID_FILE(HttpStatus.BAD_REQUEST, "E40007", "적절하지 않은 파일 내용/포맷입니다."),
  FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "E40008", "파일 크기 제한을 넘었습니다."),

  // 403 Forbidden
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "E40301", "요청에 대한 권한이 없습니다."),

  // 404 Not Found
  NOT_FOUND(HttpStatus.NOT_FOUND, "E40400", "리소스를 찾을 수 없습니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "E40401", "해당 사용자를 찾을 수 없습니다."),
  GARDEN_NOT_FOUND(HttpStatus.NOT_FOUND, "E40402", "해당 텃밭을 찾을 수 없습니다."),

  // 417 Expectation Failed
  UPLOAD_FAILED(HttpStatus.EXPECTATION_FAILED, "E41701", "파일 업로드에 실패했습니다."),

  // === 5xx Server Errors ===
  // 500 Internal Server Error
  AI_AVATAR_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E50001", "아바타 생성에 실패했습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
