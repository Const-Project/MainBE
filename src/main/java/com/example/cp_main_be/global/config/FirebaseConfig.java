package com.example.cp_main_be.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

@Configuration
public class FirebaseConfig {

  private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

  @Value("${firebase.key-base64:#{null}}")
  private String firebaseKeyBase64;

  @PostConstruct
  public void initialize() {
    log.info("=================================================================");
    log.info("[DEBUG_FIREBASE] FirebaseConfig 초기화 시작");

    try {
      if (FirebaseApp.getApps().isEmpty()) {
        InputStream serviceAccount;

        // 1. 환경변수 값 확인
        if (StringUtils.hasText(firebaseKeyBase64)) {
          log.info("[DEBUG_FIREBASE] 환경변수(firebase.key-base64) 발견됨.");
          log.info("[DEBUG_FIREBASE] 길이: {}", firebaseKeyBase64.length());
          // 보안상 앞 10자리만 출력하여 값 검증 (Base64라면 ewogIC... 로 시작해야 함)
          log.info(
              "[DEBUG_FIREBASE] 앞 10글자: {}",
              firebaseKeyBase64.substring(0, Math.min(firebaseKeyBase64.length(), 10)));

          try {
            // 2. 디코딩 시도
            byte[] decodedBytes = Base64.getDecoder().decode(firebaseKeyBase64.trim());
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);

            log.info("[DEBUG_FIREBASE] Base64 디코딩 성공.");
            // 디코딩 된 문자열의 앞부분 출력 (JSON인지 확인)
            log.info(
                "[DEBUG_FIREBASE] 디코딩된 내용(앞 50자): {}",
                decodedString.substring(0, Math.min(decodedString.length(), 50)));

            if (!decodedString.trim().startsWith("{")) {
              log.error("[DEBUG_FIREBASE] 🚨 치명적 오류: 디코딩된 내용이 '{' (JSON)로 시작하지 않습니다!");
            }

            serviceAccount = new ByteArrayInputStream(decodedBytes);

          } catch (IllegalArgumentException e) {
            log.error("[DEBUG_FIREBASE] 🚨 Base64 디코딩 실패! 환경변수 값이 올바른 Base64 형식이 아닙니다.", e);
            throw e;
          }

        } else {
          log.info("[DEBUG_FIREBASE] 환경변수 없음 -> 로컬 파일 모드로 전환");
          ClassPathResource resource =
              new ClassPathResource("napulnapul-d9572-firebase-adminsdk-fbsvc-0a2757f074.json");

          if (!resource.exists()) {
            log.error("[DEBUG_FIREBASE] 🚨 로컬 파일도 존재하지 않습니다: {}", resource.getPath());
          }
          serviceAccount = resource.getInputStream();
        }

        FirebaseOptions options =
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
        log.info("[DEBUG_FIREBASE] FirebaseApp 초기화 완료!");
      }
    } catch (Exception e) {
      log.error("[DEBUG_FIREBASE] 🚨 초기화 중 예외 발생", e);
      // 배포 로그 확인을 위해 에러를 다시 던짐
      throw new RuntimeException("Firebase 초기화 실패", e);
    }
    log.info("=================================================================");
  }
}
