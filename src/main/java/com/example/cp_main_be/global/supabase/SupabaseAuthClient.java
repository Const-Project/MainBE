package com.example.cp_main_be.global.supabase;

import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SupabaseAuthClient {

  private final WebClient webClient;

  @Value("${supabase.url}")
  private String supabaseUrl;

  @Value("${supabase.service-key}")
  private String supabaseServiceKey;

  public SupabaseUserResponse fetchUser(String accessToken) {
    if (accessToken == null || accessToken.isBlank()) {
      throw new CustomApiException(ErrorCode.INVALID_REQUEST);
    }

    return webClient
        .get()
        .uri(supabaseUrl + "/auth/v1/user")
        .header("apikey", supabaseServiceKey)
        .header("Authorization", "Bearer " + accessToken)
        .retrieve()
        .onStatus(
            status -> status.value() == 401 || status.value() == 403,
            response -> Mono.error(new CustomApiException(ErrorCode.INVALID_TOKEN)))
        .onStatus(
            HttpStatusCode::isError,
            response -> Mono.error(new CustomApiException(ErrorCode.INVALID_REQUEST)))
        .bodyToMono(SupabaseUserResponse.class)
        .block(Duration.ofSeconds(5));
  }
}
