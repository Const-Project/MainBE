package com.example.cp_main_be.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

@Configuration
public class FirebaseConfig {

  // 1. prod 환경에서는 환경변수로 주입된 JSON 문자열을 받습니다.
  // 2. local 환경에서는 이 값이 없으므로 null이 들어갑니다. (:#{null} 설정 덕분)
  @Value("${firebase.key-json:#{null}}")
  private String firebaseKeyJson;

  @PostConstruct
  public void initialize() {
    try {
      if (FirebaseApp.getApps().isEmpty()) {
        InputStream serviceAccount;

        // [분기 처리] 환경변수(JSON 문자열)가 존재하면 -> Prod/Railway 환경
        if (StringUtils.hasText(firebaseKeyJson)) {
          serviceAccount =
              new ByteArrayInputStream(firebaseKeyJson.getBytes(StandardCharsets.UTF_8));
        }
        // [분기 처리] 없으면 -> Local 개발 환경 (파일 사용)
        else {
          ClassPathResource resource =
              new ClassPathResource("napulnapul-d9572-firebase-adminsdk-fbsvc-0a2757f074.json");
          serviceAccount = resource.getInputStream();
        }

        FirebaseOptions options =
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
      }
    } catch (IOException e) {
      throw new RuntimeException("Firebase 초기화 실패", e);
    }
  }
}
