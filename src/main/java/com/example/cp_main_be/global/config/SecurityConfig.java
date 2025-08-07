package com.example.cp_main_be.global.config;

import com.example.cp_main_be.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/api/v1/register",
                        "/api/v1/auth/refresh",
                        "/swagger-ui/**", // Swagger UI 페이지
                        "/v3/api-docs/**", // OpenAPI 명세서
                        "/swagger-resources/**")
                    .permitAll() // 회원가입 및 토큰 재발급은 인증 없이 허용
                    .anyRequest()
                    .authenticated() // 그 외 모든 요청은 인증 필요
            )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
