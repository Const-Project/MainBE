package com.example.cp_main_be.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // === 4xx Client Errors ===
  // 400 Bad Request
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "E40001", "유효하지 않은 토큰입니다."),
  WATERING_COOL_DOWN(HttpStatus.ACCEPTED, "E20002", "아직 물을 줄 수 없습니다."),
  FRIEND_WATERING_LIMIT_EXCEEDED(
      HttpStatus.BAD_REQUEST, "E40003", "오늘은 다른 사람의 정원에 더 이상 물을 줄 수 없습니다."),
  ALREADY_WATERED_GARDEN(HttpStatus.ACCEPTED, "E20002", "이 정원에는 오늘 이미 물을 주었습니다."),
  SUNLIGHT_COOL_DOWN(HttpStatus.ACCEPTED, "E20002", "오늘은 이미 햇빛을 주었습니다."),
  GARDEN_SLOT_MAXED_OUT(
      HttpStatus.BAD_REQUEST, "E40006", "더 이상 텃밭을 추가할 수 없습니다."), // 최종 3개 도달 시 사용 가능
  INVALID_FILE(HttpStatus.BAD_REQUEST, "E40007", "적절하지 않은 파일 내용/포맷입니다."),
  GARDEN_SLOT_LOCKED(
      HttpStatus.BAD_REQUEST, "E40008", "현재 단계에서는 더 이상 텃밭을 만들 수 없습니다. 소원나무를 성장시켜주세요."),
  FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "E40009", "파일 크기 제한을 넘었습니다."),
  INVALID_MISSION_TYPE_FOR_QUIZ_OPTION(
      HttpStatus.BAD_REQUEST, "E40010", "퀴즈 타입의 미션에만 선지를 추가할 수 있습니다."),
  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "E40011", "잘못된 요청입니다."),
  SELF_FOLLOWING_UNABLE(HttpStatus.BAD_REQUEST, "E40012", "자신을 팔로우할 수 없습니다."),

  // 403 Forbidden
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "E40301", "요청에 대한 권한이 없습니다."),

  // 404 Not Found
  NOT_FOUND(HttpStatus.NOT_FOUND, "E40400", "리소스를 찾을 수 없습니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "E40401", "해당 사용자를 찾을 수 없습니다."),
  GARDEN_NOT_FOUND(HttpStatus.NOT_FOUND, "E40402", "해당 텃밭을 찾을 수 없습니다."),
  IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "E40403", "해당 이미지를 찾을 수 없습니다."),
  DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "E40404", "해당 일기를 찾을 수 없습니다."),
  MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "E40405", "해당 미션을 찾을 수 없습니다."),
  QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "E40406", "해당 퀴즈를 찾을 수 없습니다."),
  REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "E40407", "해당 신고를 찾을 수 없습니다."),
  AVATAR_MASTER_NOT_FOUND(HttpStatus.NOT_FOUND, "E40408", "해당 아바타 원본을 찾을 수 없습니다."),
  WISH_TREE_NOT_FOUND(HttpStatus.NOT_FOUND, "E40409", "소원나무 정보를 찾을 수 없습니다."),
  DEFAULT_RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "E40410", "필수 기본 리소스를 찾을 수 없습니다."),
  AVATAR_NOT_FOUND(HttpStatus.NOT_FOUND, "E40411", "해당 아바타를 찾을 수 없습니다."),

  // 417 Expectation Failed
  UPLOAD_FAILED(HttpStatus.EXPECTATION_FAILED, "E41701", "파일 업로드에 실패했습니다."),

  // === 5xx Server Errors ===
  // 500 Internal Server Error
  AI_AVATAR_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E50001", "아바타 생성에 실패했습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
