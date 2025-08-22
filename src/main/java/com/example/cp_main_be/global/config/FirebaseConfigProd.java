package com.example.cp_main_be.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("prod") // prod 프로필일 때만 이 설정이 활성화됨
@Configuration
public class FirebaseConfigProd {

  // application-prod.yml의 firebase.key-json 값을 주입받음
  @Value("${firebase.key-json}")
  private String firebaseKeyJson;

  @PostConstruct
  public void initialize() {
    try {
      // 주입받은 JSON 문자열을 InputStream으로 변환
      InputStream serviceAccount =
          new ByteArrayInputStream(firebaseKeyJson.getBytes(StandardCharsets.UTF_8));

      FirebaseOptions options =
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .build();

      if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options);
      }
    } catch (Exception e) {
      throw new RuntimeException("Firebase (Production) 초기화에 실패했습니다.", e);
    }
  }
}
