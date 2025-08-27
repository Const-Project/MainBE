package com.example.cp_main_be.global.jwt;

import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.common.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExceptionHandlerFilter extends OncePerRequestFilter {

  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      // 다음 필터로 요청을 넘깁니다.
      filterChain.doFilter(request, response);
    } catch (JwtAuthenticationFilter.JwtAuthenticationException e) {
      // JWT 관련 오류는 ErrorCode의 상태 코드로 처리
      log.error("JWT Authentication error: {}", e.getMessage());
      ErrorCode errorCode = e.getErrorCode();
      setErrorResponse(
          response, errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage());
    } catch (Exception e) {
      // 기타 예외는 500으로 처리
      log.error("Unhandled exception caught in filter chain: {}", e.getMessage());
      setErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  private void setErrorResponse(
      HttpServletResponse response, HttpStatus status, String code, String message)
      throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    // ErrorResponse DTO를 사용하여 JSON 응답을 생성합니다.
    ErrorResponse errorResponse = ErrorResponse.of(status, message);

    // ObjectMapper를 사용하여 DTO를 JSON 문자열로 변환하고 응답에 씁니다.
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }

  // 기존 메서드도 유지 (기타 예외용)
  private void setErrorResponse(HttpServletResponse response, HttpStatus status, String message)
      throws IOException {
    setErrorResponse(response, status, null, message);
  }
}
