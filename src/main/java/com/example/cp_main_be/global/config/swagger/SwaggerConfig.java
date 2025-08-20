package com.example.cp_main_be.global.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(title = "CP Main API", version = "v1", description = "헤더 + 쿠키 기반 JWT 인증 API 문서"))
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .components(
            new Components()
                // Bearer 헤더 인증
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Bearer Token 인증"))
                // 쿠키 인증
                .addSecuritySchemes(
                    "cookieAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("accessToken")
                        .description("JWT Cookie 인증 (accessToken)")))
        // 두 인증 방식 모두 문서에 등록
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .addSecurityItem(new SecurityRequirement().addList("cookieAuth"));
  }
}
