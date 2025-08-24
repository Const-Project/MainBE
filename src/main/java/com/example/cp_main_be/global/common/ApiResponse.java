package com.example.cp_main_be.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @JsonProperty("isSuccess")
    private boolean isSuccess;

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("result")
    private final T result;

    public static <T> ApiResponse<T> ok(T result) {
      return onSuccess(GeneralSuccessCode.OK, result);
    }

  public static <T> ApiResponse<T> success(T result) {
    return onSuccess(GeneralSuccessCode.OK, result);
  }

  public static <T> ApiResponse<T> created(T result) {
      return onSuccess(GeneralSuccessCode.CREATED, result);
    }

    public static <T> ApiResponse<T> onSuccess(BaseSuccessCode code, T result) {
      return new ApiResponse<>(true, code.getCode(), code.getMessage(), result);
    }

    public static <T> ApiResponse<T> onFailure(String code, String message) {
      return onFailure(code, message, null);
    }

    public static <T> ApiResponse<T> onFailure(String code, String message, T result) {
      return new ApiResponse<>(false, code, message, result);
    }

}
