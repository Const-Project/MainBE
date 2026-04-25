package com.example.cp_main_be.domain.avatar.image.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiGenerateResponse(List<GeminiCandidate> candidates) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record GeminiCandidate(GeminiContent content) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record GeminiContent(List<GeminiPart> parts) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record GeminiPart(String text, GeminiInlineData inlineData) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record GeminiInlineData(String mimeType, String data) {}
}
