package com.example.cp_main_be.domain.avatar.image.service;

import com.example.cp_main_be.domain.avatar.image.dto.GeminiGenerateRequest;
import com.example.cp_main_be.domain.avatar.image.dto.GeminiGenerateResponse;
import com.example.cp_main_be.domain.avatar.image.dto.ReplicatePredictionResponse;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import com.example.cp_main_be.global.infra.StorageService;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ImageProcessingService {

  private static final Logger log = LoggerFactory.getLogger(ImageProcessingService.class);
  private static final String REFERENCE_IMAGE_PATH = "avatar/reference-lowpoly-plant.png";
  private static final String GEMINI_PROMPT =
      "이 레퍼런스 이미지와 사용자가 업로드한 이미지를 함께 참고해서,"
          + " 사용자가 업로드한 이미지의 주요 피사체를 레퍼런스와 동일한 로우폴리 3D 식물/아바타 스타일로 변환해줘."
          + " 정사각형 1024x1024 이미지로 만들고, 피사체가 중앙에 오도록 해줘."
          + " 배경은 단순하게 포함해도 되지만, 이후 배경 제거가 잘 되도록 피사체 윤곽을 명확하게 만들어줘.";

  private static final Random random = new Random();
  private static final List<String> FALLBACK_IMAGE_URLS =
      List.of(
          "https://pub-5cb74645f3e1443686dd7aa7096913f1.r2.dev/%E1%84%86%E1%85%A9%E1%86%AB%E1%84%89%E1%85%B3%E1%84%90%E1%85%A6%E1%84%85%E1%85%A1%201.png",
          "https://pub-5cb74645f3e1443686dd7aa7096913f1.r2.dev/%E1%84%87%E1%85%A2%E1%86%A8%E1%84%83%E1%85%A9%E1%84%89%E1%85%A5%E1%86%AB%201.png",
          "https://pub-5cb74645f3e1443686dd7aa7096913f1.r2.dev/%E1%84%87%E1%85%A2%E1%86%BC%E1%84%80%E1%85%A1%E1%86%AF%E1%84%80%E1%85%A9%E1%84%86%E1%85%AE%E1%84%82%E1%85%A1%E1%84%86%E1%85%AE%201.png",
          "https://pub-5cb74645f3e1443686dd7aa7096913f1.r2.dev/%E1%84%89%E1%85%A1%E1%86%AB%E1%84%89%E1%85%A6%E1%84%87%E1%85%A6%E1%84%85%E1%85%B5%E1%84%8B%E1%85%A1%201.png",
          "https://pub-5cb74645f3e1443686dd7aa7096913f1.r2.dev/%E1%84%89%E1%85%A5%E1%84%8B%E1%85%A3%E1%86%BC%E1%84%85%E1%85%A1%201.png",
          "https://pub-5cb74645f3e1443686dd7aa7096913f1.r2.dev/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B5%E1%86%AB%E1%84%83%E1%85%A9%E1%86%B8%E1%84%89%E1%85%A5%E1%84%89%E1%85%B3%201.png",
          "https://pub-5cb74645f3e1443686dd7aa7096913f1.r2.dev/%E1%84%91%E1%85%A1%E1%84%8F%E1%85%B5%E1%84%85%E1%85%A1%201.png",
          "https://pub-5cb74645f3e1443686dd7aa7096913f1.r2.dev/%E1%84%92%E1%85%A2%E1%86%BC%E1%84%8B%E1%85%AE%E1%86%AB%E1%84%86%E1%85%A9%E1%86%A8%201.png");

  private final WebClient webClient;
  private final StorageService storageService;

  @Value("${google.gemini.api-key}")
  private String geminiApiKey;

  @Value("${google.gemini.image-model:gemini-2.5-flash-preview-image-generation}")
  private String geminiModel;

  @Value("${google.gemini.base-url:https://generativelanguage.googleapis.com}")
  private String geminiBaseUrl;

  @Value("${replicate.api.token}")
  private String replicateApiToken;

  @Value("${replicate.rembg.model:cjwbw/rembg}")
  private String rembgModel;

  @Value("${replicate.rembg.version:}")
  private String rembgVersion;

  @Value("${replicate.api.base-url:https://api.replicate.com}")
  private String replicateBaseUrl;

  @Value("${avatar.generation.timeout-seconds:300}")
  private int timeoutSeconds;

  @Value("${avatar.reference.validate:true}")
  private boolean validateReference;

  @PostConstruct
  void validateReferenceImage() {
    if (!validateReference) return;
    if (!new ClassPathResource(REFERENCE_IMAGE_PATH).exists()) {
      throw new IllegalStateException(
          "Reference image not found at classpath:"
              + REFERENCE_IMAGE_PATH
              + ". Place the file at src/main/resources/"
              + REFERENCE_IMAGE_PATH);
    }
  }

  public String processImageWithAi(MultipartFile imageFile) {
    try {
      log.info(
          "[AVATAR_AI] stage=process_start originalFilename={}, contentType={}, size={}",
          imageFile.getOriginalFilename(),
          imageFile.getContentType(),
          imageFile.getSize());

      byte[] userImageBytes = imageFile.getBytes();
      String userMimeType = imageFile.getContentType();
      log.info(
          "[AVATAR_AI] stage=user_image_loaded bytes={}, mimeType={}",
          userImageBytes.length,
          userMimeType);

      byte[] referenceImageBytes =
          new ClassPathResource(REFERENCE_IMAGE_PATH).getInputStream().readAllBytes();
      log.info(
          "[AVATAR_AI] stage=reference_image_loaded path={}, bytes={}",
          REFERENCE_IMAGE_PATH,
          referenceImageBytes.length);

      byte[] generatedImageBytes = callGemini(userImageBytes, userMimeType, referenceImageBytes);
      log.info("[AVATAR_AI] stage=gemini_completed bytes={}", generatedImageBytes.length);

      byte[] rembgBytes = callRembgAndDownload(generatedImageBytes);
      log.info("[AVATAR_AI] stage=rembg_completed bytes={}", rembgBytes.length);

      String imageUrl =
          storageService.uploadFile(rembgBytes, "avatars/", imageFile.getOriginalFilename());
      log.info("[AVATAR_AI] stage=storage_upload_completed imageUrl={}", imageUrl);
      return imageUrl;
    } catch (CustomApiException e) {
      log.warn(
          "[AVATAR_AI] stage=process_failed errorType=CustomApiException message={}",
          e.getMessage());
      throw e;
    } catch (Exception e) {
      log.warn(
          "[AVATAR_AI] stage=process_failed errorType={} message={}",
          e.getClass().getSimpleName(),
          e.getMessage(),
          e);
      throw new CustomApiException(ErrorCode.AI_AVATAR_FAILED);
    }
  }

  private byte[] callGemini(byte[] userImageBytes, String userMimeType, byte[] referenceBytes) {
    log.info(
        "[AVATAR_AI] stage=gemini_request_start model={}, userBytes={}, referenceBytes={}, mimeType={}",
        geminiModel,
        userImageBytes.length,
        referenceBytes.length,
        userMimeType);

    String userBase64 = Base64.getEncoder().encodeToString(userImageBytes);
    String refBase64 = Base64.getEncoder().encodeToString(referenceBytes);

    var request =
        new GeminiGenerateRequest(
            List.of(
                new GeminiGenerateRequest.GeminiContent(
                    List.of(
                        GeminiGenerateRequest.GeminiPart.image(userMimeType, userBase64),
                        GeminiGenerateRequest.GeminiPart.image("image/png", refBase64),
                        GeminiGenerateRequest.GeminiPart.text(GEMINI_PROMPT)))),
            new GeminiGenerateRequest.GeminiGenerationConfig(List.of("IMAGE")));

    GeminiGenerateResponse response =
        webClient
            .post()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .scheme(geminiBaseUrl.startsWith("http://") ? "http" : "https")
                        .host(extractHost(geminiBaseUrl))
                        .port(extractPort(geminiBaseUrl))
                        .path("/v1beta/models/{model}:generateContent")
                        .queryParam("key", geminiApiKey)
                        .build(geminiModel))
            .bodyValue(request)
            .retrieve()
            .onStatus(
                HttpStatusCode::isError,
                clientResponse ->
                    clientResponse
                        .bodyToMono(String.class)
                        .flatMap(
                            body -> {
                              log.error(
                                  "Gemini API error. Status: {}, Body: {}",
                                  clientResponse.statusCode(),
                                  body);
                              return Mono.error(new CustomApiException(ErrorCode.AI_AVATAR_FAILED));
                            }))
            .bodyToMono(GeminiGenerateResponse.class)
            .timeout(Duration.ofSeconds(60))
            .block();

    log.info(
        "[AVATAR_AI] stage=gemini_response_received hasResponse={}, candidateCount={}",
        response != null,
        response != null && response.candidates() != null ? response.candidates().size() : null);

    if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
      log.error("[AVATAR_AI] stage=gemini_failed reason=null_or_empty_candidates");
      throw new CustomApiException(ErrorCode.AI_AVATAR_FAILED);
    }

    return response.candidates().stream()
        .filter(c -> c.content() != null && c.content().parts() != null)
        .flatMap(c -> c.content().parts().stream())
        .filter(p -> p.inlineData() != null && p.inlineData().data() != null)
        .findFirst()
        .map(p -> Base64.getDecoder().decode(p.inlineData().data()))
        .orElseThrow(
            () -> {
              log.error("[AVATAR_AI] stage=gemini_failed reason=no_image_part");
              return new CustomApiException(ErrorCode.AI_AVATAR_FAILED);
            });
  }

  private byte[] callRembgAndDownload(byte[] imageBytes) {
    log.info(
        "[AVATAR_AI] stage=rembg_create_request_start baseUrl={}, model={}, versionPresent={}, inputBytes={}",
        replicateBaseUrl,
        rembgModel,
        rembgVersion != null && !rembgVersion.isBlank(),
        imageBytes.length);

    String dataUri = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);

    Object requestBody =
        (rembgVersion != null && !rembgVersion.isBlank())
            ? Map.of("version", rembgVersion, "input", Map.of("image", dataUri))
            : Map.of("input", Map.of("image", dataUri));

    String predictionsUri =
        (rembgVersion != null && !rembgVersion.isBlank())
            ? replicateBaseUrl + "/v1/predictions"
            : replicateBaseUrl + "/v1/models/" + rembgModel + "/predictions";
    log.info(
        "[AVATAR_AI] stage=rembg_create_request_uri uri={}, versionPresent={}",
        predictionsUri,
        rembgVersion != null && !rembgVersion.isBlank());

    ReplicatePredictionResponse prediction =
        webClient
            .post()
            .uri(predictionsUri)
            .header("Authorization", "Bearer " + replicateApiToken)
            .bodyValue(requestBody)
            .retrieve()
            .onStatus(
                HttpStatusCode::isError,
                clientResponse ->
                    clientResponse
                        .bodyToMono(String.class)
                        .flatMap(
                            body -> {
                              log.error(
                                  "Replicate create prediction error. Status: {}, Body: {}",
                                  clientResponse.statusCode(),
                                  body);
                              return Mono.error(new CustomApiException(ErrorCode.AI_AVATAR_FAILED));
                            }))
            .bodyToMono(ReplicatePredictionResponse.class)
            .timeout(Duration.ofSeconds(30))
            .block();

    if (prediction == null || prediction.id() == null) {
      log.error("[AVATAR_AI] stage=rembg_create_failed reason=null_or_empty_id");
      throw new CustomApiException(ErrorCode.AI_AVATAR_FAILED);
    }
    log.info(
        "[AVATAR_AI] stage=rembg_prediction_created predictionId={}, status={}",
        prediction.id(),
        prediction.status());

    String outputUrl =
        fetchPrediction(prediction.id())
            .flatMap(
                r -> {
                  String status = r.status();
                  if ("succeeded".equals(status)) return Mono.just(r);
                  if ("failed".equals(status) || "canceled".equals(status)) {
                    log.error(
                        "Replicate prediction {} 상태: {}, error: {}", r.id(), status, r.error());
                    return Mono.error(new CustomApiException(ErrorCode.AI_AVATAR_FAILED));
                  }
                  return Mono.empty();
                })
            .repeatWhenEmpty(flux -> flux.delayElements(Duration.ofSeconds(2)))
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .map(ReplicatePredictionResponse::output)
            .block();

    if (outputUrl == null || outputUrl.isBlank()) {
      log.error(
          "[AVATAR_AI] stage=rembg_failed reason=empty_output_url predictionId={}",
          prediction.id());
      throw new CustomApiException(ErrorCode.AI_AVATAR_FAILED);
    }
    log.info(
        "[AVATAR_AI] stage=rembg_output_ready predictionId={}, outputUrl={}",
        prediction.id(),
        outputUrl);

    byte[] resultBytes =
        webClient
            .get()
            .uri(outputUrl)
            .header("Authorization", "Bearer " + replicateApiToken)
            .retrieve()
            .bodyToMono(byte[].class)
            .timeout(Duration.ofSeconds(60))
            .block();

    if (resultBytes == null || resultBytes.length == 0) {
      log.error(
          "[AVATAR_AI] stage=rembg_download_failed reason=empty_result predictionId={}",
          prediction.id());
      throw new CustomApiException(ErrorCode.AI_AVATAR_FAILED);
    }
    log.info(
        "[AVATAR_AI] stage=rembg_download_completed predictionId={}, bytes={}",
        prediction.id(),
        resultBytes.length);

    return resultBytes;
  }

  private Mono<ReplicatePredictionResponse> fetchPrediction(String predictionId) {
    return webClient
        .get()
        .uri(replicateBaseUrl + "/v1/predictions/" + predictionId)
        .header("Authorization", "Bearer " + replicateApiToken)
        .retrieve()
        .bodyToMono(ReplicatePredictionResponse.class);
  }

  public String getDefaultImageUrl() {
    return FALLBACK_IMAGE_URLS.get(random.nextInt(FALLBACK_IMAGE_URLS.size()));
  }

  private String extractHost(String baseUrl) {
    String withoutScheme = baseUrl.replaceFirst("https?://", "");
    int colonIdx = withoutScheme.indexOf(':');
    return colonIdx == -1 ? withoutScheme : withoutScheme.substring(0, colonIdx);
  }

  private int extractPort(String baseUrl) {
    String withoutScheme = baseUrl.replaceFirst("https?://", "");
    int colonIdx = withoutScheme.indexOf(':');
    if (colonIdx == -1) return -1;
    try {
      return Integer.parseInt(withoutScheme.substring(colonIdx + 1));
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}
