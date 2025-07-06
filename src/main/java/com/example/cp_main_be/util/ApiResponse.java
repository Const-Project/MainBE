package com.example.cp_main_be.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // null인 필드는 JSON 응답에 포함하지 않음
public class ApiResponse<T> {

  private final boolean success;
  private final T data;
  private final ApiError error;

  // 성공 시 생성자
  private ApiResponse(boolean success, T data) {
    this.success = success;
    this.data = data;
    this.error = null;
  }

  // 실패 시 생성자
  private ApiResponse(boolean success, ApiError error) {
    this.success = success;
    this.data = null;
    this.error = error;
  }

  // 성공 응답 생성 정적 메소드
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, data);
  }

  // 실패 응답 생성 정적 메소드
  public static <T> ApiResponse<T> failure(String code, String message) {
    return new ApiResponse<>(false, new ApiError(code, message));
  }

  @Getter
  private static class ApiError {
    private final String code;
    private final String message;

    private ApiError(String code, String message) {
      this.code = code;
      this.message = message;
    }
  }
}
