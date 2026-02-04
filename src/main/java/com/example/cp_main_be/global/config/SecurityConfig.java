package com.example.cp_main_be.global.config;

import com.example.cp_main_be.global.jwt.ExceptionHandlerFilter;
import com.example.cp_main_be.global.jwt.JwtAuthenticationFilter;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final ExceptionHandlerFilter exceptionHandlerFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
        .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/api/v1/auth/signup",
                        "/api/v1/auth/supabase",
                        "/api/v1/auth/refresh",
                        "/api/v1/policy",
                        "/swagger-ui/**", // Swagger UI 페이지
                        "/v3/api-docs/**", // OpenAPI 명세서
                        "/swagger-resources/**",
                        "/h2-console/**",
                        "/error",
                        "/api/v1/realQuiz/**",
                        "/api/v1/realQuiz")
                    .permitAll() // 회원가입 및 토큰 재발급은 인증 없이 허용
                    .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated() // 그 외 모든 요청은 인증 필요
            )
        .addFilterBefore(exceptionHandlerFilter, LogoutFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // 모든 출처 허용 (개발 환경)
    configuration.addAllowedOriginPattern("*");

    // 모든 HTTP 메서드 허용
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

    // 모든 헤더 허용
    configuration.addAllowedHeader("*");

    // 인증 정보 포함 허용 (쿠키, Authorization 헤더 등)
    configuration.setAllowCredentials(true);

    // preflight 요청 캐시 시간 (초)
    configuration.setMaxAge(3600L);

    // 응답에서 클라이언트가 접근할 수 있는 헤더 설정
    configuration.addExposedHeader("Authorization");
    configuration.addExposedHeader("Content-Type");
    configuration.addExposedHeader("Accept");

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
