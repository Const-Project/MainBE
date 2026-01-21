package com.example.cp_main_be.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GeneralSuccessCode implements BaseSuccessCode {
  OK(HttpStatus.OK, "COMMON2000", "성공적으로 처리했습니다."),
  CREATE_SUCCESS(HttpStatus.CREATED, "COMMON2010", "성공적으로 생성했습니다."),
  DELETE_SUCCESS(HttpStatus.OK, "COMMON2000", "성공적으로 삭제했습니다."),
  CREATED(HttpStatus.CREATED, "COMMON2010", "성공적으로 생성했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
