package com.example.cp_main_be.global.supabase;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SupabaseUserResponse {

  private String id;
  private String email;

  @JsonProperty("app_metadata")
  private Map<String, Object> appMetadata;

  @JsonProperty("user_metadata")
  private Map<String, Object> userMetadata;
}
