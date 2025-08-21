package com.example.cp_main_be.global.jwt;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    logger.info("JwtAuthenticationFilter is running for URI: {}", request.getRequestURI());
    String token = getJwtFromRequest(request);

    if (token == null) {
      logger.warn("Token is null. No Authorization header or accessToken cookie found.");
    } else {
      logger.info("Token found: {}", token);
      if (jwtTokenProvider.validateToken(token)) {
        logger.info("Token validation successful.");
        String uuidStr = jwtTokenProvider.getUuidFromToken(token); // uuid를 가져오는 로직이 필요합니다.
        UUID uuid = UUID.fromString(uuidStr);

        User user =
            userRepository
                .findByUuid(uuid)
                .orElseThrow(
                    () -> new UsernameNotFoundException("User not found with uuid: " + uuid));

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                user,
                null,
                java.util.Collections.singletonList(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(authentication);
      } else {
        logger.warn("Token validation FAILED.");
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
}
