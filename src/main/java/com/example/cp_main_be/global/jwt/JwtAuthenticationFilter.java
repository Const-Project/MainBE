package com.example.cp_main_be.global.jwt;

import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.user.service.CustomUserDetailsService;
import com.example.cp_main_be.global.common.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private final CustomUserDetailsService customUserDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    logger.info("JwtAuthenticationFilter is running for URI: {}", request.getRequestURI());
    String token = getJwtFromRequest(request);

    if (token == null) {
      logger.warn("Token is null. No Authorization header or accessToken cookie found.");
    } else {
      logger.info("Token found.");

      try {
        // validateToken에서 예외가 발생하면 바로 catch로 넘어감
        if (jwtTokenProvider.validateToken(token)) {
          logger.info("Token validation successful.");
          String uuidStr = jwtTokenProvider.getUuidFromToken(token);

          // UserDetailsService를 통해 사용자 정보 로드
          UserDetails userDetails = customUserDetailsService.loadUserByUsername(uuidStr);

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                  userDetails, // Principal로 UserDetails 객체 사용
                  null,
                  userDetails.getAuthorities()); // UserDetails에서 직접 권한 목록을 가져옴

          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } catch (Exception e) {
        logger.warn("Token validation failed: {}", e.getMessage());
        // 모든 토큰 관련 오류를 INVALID_TOKEN으로 처리
        throw new JwtAuthenticationException(ErrorCode.INVALID_TOKEN, e);
      }
    }

    // if-else 블록 바깥에서 항상 실행되어야 합니다.
    filterChain.doFilter(request, response);
  }

  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    // 쿠키에서 토큰 찾기
    if (request.getCookies() != null) {
      return Arrays.stream(request.getCookies())
          .filter(cookie -> "accessToken".equals(cookie.getName()))
          .map(Cookie::getValue)
          .findFirst()
          .orElse(null);
    }
    return null;
  }

  // 커스텀 예외 클래스
  public static class JwtAuthenticationException extends RuntimeException {
    private final ErrorCode errorCode;

    public JwtAuthenticationException(ErrorCode errorCode, Throwable cause) {
      super(errorCode.getMessage(), cause);
      this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
      return errorCode;
    }
  }
}
