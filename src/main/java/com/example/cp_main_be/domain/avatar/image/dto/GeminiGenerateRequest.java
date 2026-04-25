package com.example.cp_main_be.domain.avatar.image.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public record GeminiGenerateRequest(
    List<GeminiContent> contents, GeminiGenerationConfig generationConfig) {

  public record GeminiContent(List<GeminiPart> parts) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record GeminiPart(String text, GeminiInlineData inlineData) {
    public static GeminiPart text(String text) {
      return new GeminiPart(text, null);
    }

    public static GeminiPart image(String mimeType, String base64Data) {
      return new GeminiPart(null, new GeminiInlineData(mimeType, base64Data));
    }
  }

  public record GeminiInlineData(String mimeType, String data) {}

  public record GeminiGenerationConfig(List<String> responseModalities) {}
}
