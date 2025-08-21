package com.example.cp_main_be.global.jwt;

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
    } catch (Exception e) {
      // 어떤 예외든 여기서 처리합니다.
      log.error("Unhandled exception caught in filter chain: {}", e.getMessage());
      setErrorResponse(response, e);
    }
  }

  private void setErrorResponse(HttpServletResponse response, Exception ex) throws IOException {
    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    // ErrorResponse DTO를 사용하여 JSON 응답을 생성합니다.
    ErrorResponse errorResponse =
        ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage() // 실제 예외 메시지를 포함
            );

    // ObjectMapper를 사용하여 DTO를 JSON 문자열로 변환하고 응답에 씁니다.
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
