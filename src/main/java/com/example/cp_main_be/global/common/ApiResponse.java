package com.example.cp_main_be.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  private final T data;

  private ApiResponse(T data) {
    this.data = data;
  }

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(data);
  }
}
