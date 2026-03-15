package com.example.cp_main_be.global.jwt;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.user.service.CustomUserDetailsService;
import com.example.cp_main_be.global.common.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final List<String> NICKNAME_SETUP_ALLOWED_PATHS =
      List.of(
          "/api/v1/users/me/nickname",
          "/api/v1/users/me",
          "/api/v1/auth/refresh",
          "/api/v1/auth/supabase",
          "/api/v1/policy",
          "/error");

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private final CustomUserDetailsService customUserDetailsService;

  @Value("${user.last-accessed.update-minutes:10}")
  private long lastAccessedUpdateMinutes;

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
        if (jwtTokenProvider.validateToken(token)) {
          logger.info("Token validation successful.");
          String uuidStr = jwtTokenProvider.getUuidFromToken(token);
          UUID uuid = UUID.fromString(uuidStr);

          UserDetails userDetails = customUserDetailsService.loadUserByUsername(uuidStr);
          User user =
              userRepository
                  .findByUuid(uuid)
                  .orElseThrow(
                      () ->
                          new JwtAuthenticationException(
                              ErrorCode.USER_NOT_FOUND,
                              new IllegalStateException("사용자를 찾을 수 없습니다.")));

          // 한글 주석:
          // 소셜 신규 계정은 닉네임 확정 전까지 서비스의 다른 쓰기/조회 흐름으로 빠지면
          // 임시 닉네임이 노출될 수 있으므로 닉네임 저장 API 외 접근을 차단한다.
          if (user.requiresNicknameSetup() && !isNicknameSetupAllowedRequest(request)) {
            throw new JwtAuthenticationException(
                ErrorCode.ACCESS_DENIED,
                new IllegalStateException("닉네임 설정이 완료되지 않은 계정입니다."));
          }

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());

          SecurityContextHolder.getContext().setAuthentication(authentication);

          LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
          LocalDateTime lastAccessedAt = user.getLastAccessedAt();
          if (lastAccessedAt == null
              || lastAccessedAt.isBefore(now.minusMinutes(lastAccessedUpdateMinutes))) {
            user.updateLastAccessedAt(now);
            userRepository.save(user);
          }
        }
      } catch (JwtAuthenticationException e) {
        throw e;
      } catch (Exception e) {
        logger.warn("Token validation failed: {}", e.getMessage());
      }
    }

    filterChain.doFilter(request, response);
  }

  private boolean isNicknameSetupAllowedRequest(HttpServletRequest request) {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }

    String requestUri = request.getRequestURI();
    return NICKNAME_SETUP_ALLOWED_PATHS.stream().anyMatch(requestUri::startsWith);
  }

  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    if (request.getCookies() != null) {
      return Arrays.stream(request.getCookies())
          .filter(cookie -> "accessToken".equals(cookie.getName()))
          .map(Cookie::getValue)
          .findFirst()
          .orElse(null);
    }
    return null;
  }

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
